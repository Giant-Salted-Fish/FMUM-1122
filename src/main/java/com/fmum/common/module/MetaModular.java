package com.fmum.common.module;

import java.util.HashMap;
import java.util.Set;

import javax.annotation.Nullable;

import com.fmum.common.FMUM;
import com.fmum.common.meta.MetaGrouped;
import com.fmum.common.util.CoordSystem;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

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
	
	public static final String TAG = "0";
	
	public static final double[] DEF_OFFSETS = { 0D };
	
	public static final ModuleSlot[] DEF_SLOTS = { };
	
	public static final PreInstalledModules DEF_DEF_MODULES = new PreInstalledOnRail( null );
	
	@Override
	public default void regisPostInitHandler( Set< Runnable > tasks )
	{
		MetaGrouped.super.regisPostInitHandler( tasks );
		
		tasks.add( () -> this.regisTo( this, regis ) );
		
		// Register it into id map. In default uses the hash value of its name.
		tasks.add(
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
		tasks.add( () -> {
			for( int i = this.numSlots(); i-- > 0; this.slot( i ).rescale( this.modelScale() ) );
			
			// TODO: scale the hit box
		} );
	}
	
	@Override
	public default void regisPostLoadHandler( Set< Runnable > tasks ) {
		MetaGrouped.super.regisPostLoadHandler( tasks );
	}
	
	public default int id() { return idMapping.inverse().get( this ); }
	
	/**
	 * Generate tag for this module to install into a slot
	 * 
	 * @param dam Damage of the module
	 */
	public default NBTTagList genTag( int dam )
	{
		// Initialize data
		final int[] data = new int[ this.dataArraySize() ];
		data[ 0 ] = ( this.id() << 16 ) + ( 0xFFFF & dam );
		
		// Create tag and data tag
		final NBTTagList dataWrapper = new NBTTagList();
		dataWrapper.appendTag( new NBTTagIntArray( data ) );
		final NBTTagList tag = new NBTTagList();
		tag.appendTag( dataWrapper );
		
		// Append slot tag and default modules
		for( int i = this.numSlots(); i-- > 0; tag.appendTag( new NBTTagList() ) );
		this.defModules().writeToTag( tag );
		return tag;
	}
	
	/**
	 * Notice that cross inherent could cause this method to fail if simply add up the size
	 * retrieved from super method. In that case you may need to specify the size of the data array
	 * manually.
	 * 
	 * @return The length of the data {@code int} array
	 */
	public default int dataArraySize() { return 1; }
	
	public default PreInstalledModules defModules() { return DEF_DEF_MODULES; }
	
	public default void updatePosStepX( NBTTagList tag, int step ) { }
	
	public default void updatePosStepY( NBTTagList tag, int step ) { }
	
	public default void updatePosStepZ( NBTTagList tag, int step ) { }
	
	public default void updateOffset( NBTTagList tag, int offset ) { }
	
	/**
	 * Apply transform of its position into given coordinate system
	 * 
	 * @param slot Slot that this module has been installed in
	 * @param tag Base tag of this module
	 * @param dst Where to apply transform to
	 */
	public default void applyTransform( ModuleSlot slot, NBTTagList tag, CoordSystem dst ) { }
	
	public default boolean stream( NBTTagList tag, ModuleVisitor visitor )
	{
		// Visit this module
		if( visitor.visit( tag, this, null ) ) return true;
		
		// Visit each module installed on this module
		NBTTagList slotTag, moduleTag;
		for( int i = this.numSlots(); i-- > 0; )
			for( int j = ( slotTag = ( NBTTagList ) tag.get( 1 + i ) ).tagCount(); j-- > 0; )
				if(
					this.decodeInstalled(
						moduleTag = ( NBTTagList ) slotTag.get( j )
					).stream( moduleTag, visitor )
				) return true;
		
		return false;
	}
	
	/**
	 * Stream for renderer. Difference from a normal stream is that the visitor is capable to change
	 * the position of current module and it will effect all the modules installed on this module.
	 * 
	 * @return
	 */
	public default void stream( NBTTagList tag, CoordSystem envSys, ModuleVisitor visitor )
	{
		// Visit this module
		visitor.visit( tag, this, envSys );
		
		// Check if this module has slots to iterate
		int i = this.numSlots();
		if( i == 0) return;
		
		// Fetch coordinate systems
		final CoordSystem moduleSys = CoordSystem.get();
		final CoordSystem slotSys = CoordSystem.get();
		
		// Go through each slot
		while( i-- > 0 )
		{
			// Check if there exists some installed modules
			NBTTagList slotTag = ( NBTTagList ) tag.get( 1 + i );
			if( slotTag.tagCount() == 0 ) continue;
			
			// Apply transform to coordinate system
			ModuleSlot slot = this.slot( i );
			slotSys.set( envSys );
			slot.applyTransform( slotSys );
			
			for( int j = slotTag.tagCount(); j-- > 0; )
			{
				NBTTagList moduleTag = ( NBTTagList ) slotTag.get( j );
				MetaModular moduleMeta = this.decodeInstalled( moduleTag );
				
				moduleSys.set( slotSys );
				moduleMeta.applyTransform( slot, moduleTag, moduleSys );
				moduleMeta.stream( moduleTag, moduleSys, visitor );
			}
		}
		
		slotSys.release();
		moduleSys.release();
	}
	
	public default int numOffsets() { return 1; }
	
	public default double offset( int index ) { return DEF_OFFSETS[ index ]; }
	
	public default int numSlots() { return DEF_SLOTS.length; }
	
	public default ModuleSlot slot( int index ) { return DEF_SLOTS[ index ]; }
	
	public default MetaModular decodeInstalled( NBTTagList tag ) {
		return idMapping.get( ( ( NBTTagList ) tag.get( 0 ) ).getIntArrayAt( 0 )[ 0 ] >>> 16 );
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
