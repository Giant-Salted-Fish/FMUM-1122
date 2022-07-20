package com.fmum.common.module;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.fmum.common.FMUM;
import com.fmum.common.meta.MetaGrouped;
import com.fmum.common.util.CoordSystem;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;

/**
 * <p> This interface specifies the properties of the modules that can be modified via the
 * modification system built in {@link FMUM}. Notice that all the modules installed on a module will
 * be stored into NBT tags hence anything that can carry NBT tag will be able to implement this
 * interface. </p>
 * 
 * <p> The base NBT tag of a module will be a {@link NBTTagList}. The first tag in the base tag will
 * be a {@link NBTTagList} with a single {@link NBTTagIntArray}(not strictly required by usually the
 * case) that store the meta data(such as item ID, damage, position...). Rest tags in base tag are
 * corresponding to each {@link ModuleSlot} of the module. </p>
 * 
 * <p> This interface requires one {@code int} to store its data. The length of the meta {@code int}
 * array can be changed according to the requirement. </p>
 * <pre>
 *        16               16
 * 0000000000000000 0000000000000000
 *        |                |
 *        id             damage
 * </pre>
 * 
 * <p> Notice that the default way of assigning id for the {@link MetaModular} is to use the hash of
 * its name and discard the high 16 bits of it. Hence changing the name of a module could cause
 * problem and it is recommended to not change the name unless it is necessary when you are running
 * a server or playing with a save that you do not want it to break. </p>
 * 
 * @see ModuleSlot
 * @author Giant_Salted_Fish
 */
public interface MetaModular extends MetaGrouped
{
	public static final HashMap< String, MetaModular > regis = new HashMap<>();
	
	public static final BiMap< Integer, MetaModular > idMapping = HashBiMap.create();
	
	/**
	 * Key that maps to the data for the modular in NBT tag
	 */
	public static final String TAG = "0";
	
	public static final double[] DEF_OFFSETS = { 0D };
	
	public static final ModuleSlot[] DEF_SLOTS = { };
	
	public static final PreInstalledModules DEF_MODULES = new PreInstalledOnRail( null );
	
	@Override
	public default void regisPostInitHandler( Map< String, Runnable > tasks )
	{
		MetaGrouped.super.regisPostInitHandler( tasks );
		
		tasks.put( "REGIS_MODULE", () -> this.regisTo( this, regis ) );
		
		// Register it into id map. In default uses the hash value of its name.
		tasks.put(
			"GEN_MODULE_ID",
			() -> {
				// Keep to add prefix '_' until we find a id that is not conflict
				String name = this.name();
				while( idMapping.get( 0xFFFF & name.hashCode() ) != null )
					name = "_" + name;
				
				// Register with it
				try { idMapping.put( 0xFFFF & name.hashCode(), this ); }
				catch( IllegalArgumentException e ) {
					this.log().error( "Duplicate module register <" + this + ">" );
				}
			}
		);
		
		// Apply model scale to slots and hit boxes
		tasks.put(
			"RESCALE_MODULE",
			() -> {
				for(
					int i = this.numSlots();
					i-- > 0;
					this.slot( i ).rescale( this.scale() / 16D )
				);
				
				// TODO: scale the hit box
			}
		);
	}
	
	@Override
	public default void regisPostLoadHandler( Map< String, Runnable > tasks ) {
		MetaGrouped.super.regisPostLoadHandler( tasks );
	}
	
	public default boolean stream( NBTTagList tag, ModuleVisitor visitor )
	{
		// Visit this module
		if( visitor.visit( tag, this, null ) ) return true;
		
		// Visit each module installed on this module
		NBTTagList slotTag, moduleTag;
		for( int i = this.numSlots(); i-- > 0; )
			for( int j = ( slotTag = ( NBTTagList ) tag.get( 1 + i ) ).tagCount(); j-- > 0; )
				if(
					decodeInstalled(
						moduleTag = ( NBTTagList ) slotTag.get( j )
					).stream( moduleTag, visitor )
				) return true;
		
		return false;
	}
	
	/**
	 * Stream for renderer. Difference from a normal stream is that the visitor is capable to change
	 * the position of current module and it will effect all the modules installed on this module.
	 * 
	 * @return {@code true} if visitor returns {@code true}
	 */
	public default void stream( NBTTagList tag, CoordSystem envSys, ModuleVisitor visitor )
	{
		// Visit this module
		visitor.visit( tag, this, envSys );
		
		// Check if this module has slots to iterate
		int i = this.numSlots();
		if( i == 0) return;
		
		// Fetch coordinate systems
		final CoordSystem moduleSys = CoordSystem.locate();
		final CoordSystem slotSys = CoordSystem.locate();
		
		// Go through each slot
		while( i-- > 0 )
		{
			// Check if there exists some installed modules
			NBTTagList slotTag = ( NBTTagList ) tag.get( 1 + i );
			if( slotTag.tagCount() == 0 ) continue;
			
			// Apply transform to coordinate system
			ModuleSlot slot = this.slot( i );
			slotSys.set( envSys );
			slot.apply( slotSys );
			
			for( int j = slotTag.tagCount(); j-- > 0; )
			{
				NBTTagList moduleTag = ( NBTTagList ) slotTag.get( j );
				MetaModular moduleMeta = decodeInstalled( moduleTag );
				
				moduleSys.set( slotSys );
				moduleMeta.apply( moduleTag, slot, moduleSys );
				moduleMeta.stream( moduleTag, moduleSys, visitor );
			}
		}
		
		slotSys.release();
		moduleSys.release();
	}
	
	/**
	 * Apply transform of its position into given coordinate system
	 * 
	 * @param tag Base tag of this module
	 * @param slot Slot that this module has been installed in
	 * @param dst Where to apply transform to
	 */
	public default void apply( NBTTagList tag, ModuleSlot slot, CoordSystem dst ) { }
	
	/**
	 * Generate tag for this module to install into a slot
	 */
	public default NBTTagList genTag()
	{
		// Create tag and data tag
		final NBTTagList dataWrapper = new NBTTagList();
		dataWrapper.appendTag( new NBTTagIntArray( new int[ this.dataSize() ] ) );
		final NBTTagList tag = new NBTTagList();
		tag.appendTag( dataWrapper );
		
		// Set id
		setData( tag, 0, 0, 0xFFFF, this.id() );
		
		// Append slot tag and pre-installed modules
		for( int i = this.numSlots(); i-- > 0; tag.appendTag( new NBTTagList() ) );
		this.defModules().writeToTag( tag );
		return tag;
	}
	
	public default int installedInSlot( NBTTagList tag, int slot ) {
		return ( ( NBTTagList ) tag.get( 1 + slot ) ).tagCount();
	}
	
	public default NBTTagList tag( NBTTagCompound tag ) { return ( NBTTagList ) tag.getTag( TAG ); }
	
	public default int dam( NBTTagList tag ) { return getData( tag, 0, 16, 0xFFFF ); }
	
	public default void $dam( NBTTagList tag, int dam ) { setData( tag, 0, 16, 0xFFFF, dam ); }
	
	public default int step( NBTTagList tag ) { return 0; }
	
	/**
	 * Step effects the position in an external way. In other words it just tells the step that it
	 * wants to the outside world.
	 * 
	 * @see #$offset(NBTTagList, int)
	 */
	public default void $step( NBTTagList tag, int step ) { }
	
	public default int offset( NBTTagList tag ) { return 0; }
	
	/**
	 * Offset effects the position in an internal way. In other words this will be handled by this
	 * module it self to actually change the position.
	 * 
	 * @see #$step(NBTTagList, int)
	 */
	public default void $offset( NBTTagList tag, int offset ) { }
	
	public default int id() { return idMapping.inverse().get( this ); }
	
	public default int numSlots() { return DEF_SLOTS.length; }
	
	public default ModuleSlot slot( int idx ) { return DEF_SLOTS[ idx ]; }
	
	public default int numOffsets() { return 1; }
	
	public default double offset( int idx ) { return DEF_OFFSETS[ idx ]; }
	
	public default PreInstalledModules defModules() { return DEF_MODULES; }
	
	/**
	 * Notice that diamond Inheritance could cause this method to fail if simply add up the size
	 * retrieved from all its super. In that case you may need to specify the size of the data array
	 * manually.
	 * 
	 * @return The length of the {@code int} data array
	 */
	public default int dataSize() { return 1; }
	
	/// Helper methods ///
	public static MetaModular decodeInstalled( NBTTagList tag ) {
		return idMapping.get( getData( tag, 0, 0, 0xFFFF ) );
	}
	
	/**
	 * Helps to get data in data array
	 * 
	 * @param tag Tag of the module
	 * @param index Index of the data to set
	 * @param offset Offset of the value to get
	 * @param mask Mask to be applied after offset
	 * @return Retrieved value
	 */
	public static int getData( NBTTagList tag, int index, int offset, int mask ) {
		return getDataArray( tag )[ index ] >>> offset & mask;
	}
	
	public static int getData( int[] data, int index, int offset, int mask ) {
		return data[ index ] >>> offset & mask;
	}
	
	/**
	 * Helps to set data in data array
	 * 
	 * @param tag Tag of the module
	 * @param index Index of the data to set
	 * @param offset Offset to be applied to value
	 * @param mask Mask to be applied to value
	 * @param value Value to set
	 */
	public static void setData( NBTTagList tag, int index, int offset, int mask, int value )
	{
		int[] data = getDataArray( tag );
		data[ index ] = data[ index ] & mask << offset | ( value & mask ) << offset;
	}
	
	public static int[] getDataArray( NBTTagList tag ) {
		return ( ( NBTTagList ) tag.get( 0 ) ).getIntArrayAt( 0 );
	}
	
	public static MetaModular get( String name ) { return regis.get( name ); }
	
	public static MetaModular get( Integer id ) { return idMapping.get( id ); }
	
	@FunctionalInterface
	public static interface ModuleVisitor
	{
		/**
		 * @see MetaModular#stream(NBTTagList, ModuleVisitor)
		 * @see MetaModular#stream(NBTTagList, CoordSystem, ModuleVisitor)
		 * @param tag Tag of the module to visit
		 * @param met Meta of the module to visit
		 * @param sys
		 *     Position of this module. Note that this is just a buffer and it is likely to be
		 *     changed in the future. If you want to keep it please fetch a {@link CoordSystem}
		 *     instance and copy the value of this system to it. Changing position of this system
		 *     will have effect on all modules installed on it.
		 * @return {@code true} if should stop further streaming
		 */
		public boolean visit( NBTTagList tag, MetaModular met, @Nullable CoordSystem sys );
	}
}
