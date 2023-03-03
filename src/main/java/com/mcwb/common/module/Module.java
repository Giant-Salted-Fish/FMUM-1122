package com.mcwb.common.module;

import java.util.ArrayList;
import java.util.function.Consumer;

import com.mcwb.common.MCWB;
import com.mcwb.common.item.IItem;
import com.mcwb.common.paintjob.IPaintable;
import com.mcwb.util.Mat4f;

import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * <p> This implementation of {@link IModular} uses a wrapper to satisfy the constraint of
 * {@link IItem}. </p>
 * 
 * <p> Has additional support for {@link IPaintable} for convenience </p>
 * 
 * @see ModuleWrapper
 * @author Giant_Salted_Fish
 */
public abstract class Module< T extends IModular< ? extends T > >
	implements IModular< T >, IPaintable
{
	protected static final String MODULE_TAG = "m";
	
	protected transient final Mat4f mat = new Mat4f(); // FIXME: how to handle with this?
	
	protected transient IModular< ? > base;
	protected transient short baseSlot;
	
	protected short paintjob = 0;
	
	protected final ArrayList< T > installed = new ArrayList<>();
	protected final byte[] indices = new byte[ this.slotCount() ];
	
	protected transient IModifyState modifyState = IModifyState.NOT_SELECTED;
	
	/**
	 * Bounden NBT used for data persistence
	 */
	protected transient NBTTagCompound nbt;
	
	protected Module()
	{
		this.nbt = new NBTTagCompound();
		final int[] data = new int[ this.dataSize() ];
		data[ 0 ] = this.id();
		this.nbt.setIntArray( DATA_TAG, data );
		this.nbt.setTag( MODULE_TAG, new NBTTagList() );
	}
	
	protected Module( NBTTagCompound nbt ) { this.deserializeNBT( nbt ); }
	
	@Override
	public IModular< ? > primary() { return this.base.primary(); }
	
	@Override
	public IModular< ? > base() { return this.base; }
	
	@Override
	public void setBase( IModular< ? > base, int baseSlot )
	{
		this.base = base;
		this.baseSlot = ( short ) baseSlot;
		
		// Update position for server side. Client side will always update matrix before render.
		// FIXME: matrix is not serialized hence this will not work on server side
//		this.globalMat.setIdentity();
//		base.applySlotTransform( this, this.globalMat );
	}
	
	@Override
	public void forEach( Consumer< ? super T > visitor )
	{
		this.installed.forEach( mod -> {
			visitor.accept( mod );
			mod.forEach( visitor );
		} );
	}
	
	@Override
	public void syncAndUpdate() { this.base.syncAndUpdate(); }
	
	@Override
	public void updateState()
	{
		// FIXME: Check if this works
		this.mat.setIdentity();
		this.base.applyTransform( this.baseSlot, this, this.mat );
		
		this.installed.forEach( T::updateState );
	}
	
	@Override
	public IModifyPredicate tryInstall( int slot, IModular< ? > module )
	{
		final IModular< ? > unwrapped = module.primary();
		final IModuleSlot mslot = this.getSlot( slot );
		if( !mslot.isAllowed( unwrapped ) ) return IModifyPredicate.NO_PREVIEW;
		
		final int capacity = Math.min( MCWB.maxSlotCapacity, mslot.capacity() );
		if( this.getInstalledCount( slot ) > capacity )
		{
			return ( IModifyPredicate.NotOk )
				() -> I18n.format( "mcwb.msg.arrive_max_module_capacity", capacity );
		}
		
		final IModuleModifier m0 = () -> {
			this.install( slot, unwrapped );
			return unwrapped;
		};
		final IModuleModifier m1 = this.primary().onModuleInstall( this, slot, module, m0 );
		final IModuleModifier m2 = unwrapped.onModuleInstall( this, slot, module, m1 );
		m2.action();
		return m2.predicate();
	}
	
	@Override
	public IModular< ? > removeFromBase( int slot, int idx )
	{
		final IModuleModifier modifier = () -> this.base.remove( slot, idx );
		return this.primary().onModuleRemove( this, slot, idx, modifier ).action();
	}
	
	@Override
	public IModuleModifier onModuleInstall(
		IModular< ? > base, int slot, IModular< ? > module,
		IModuleModifier modifier
	) {
		for( T mod : this.installed )
			modifier = mod.onModuleInstall( base, slot, module, modifier );
		return modifier;
	}
	
	@Override
	public IModuleModifier onModuleRemove(
		IModular< ? > base, int slot, int idx,
		IModuleModifier modifier
	) {
		for( T mod : this.installed )
			modifier = mod.onModuleRemove( base, slot, idx, modifier );
		return modifier;
	}
	
	@Override
	@SuppressWarnings( "unchecked" )
	public void install( int slot, IModular< ? > module )
	{
		final T mod = ( T ) module;
		mod.setBase( this, slot );
		
		// Update installed list
		final int idx = this.getIdx( slot + 1 );
		this.installed.add( idx, mod );
		
		// Update NBT tag
		final NBTTagList modList = this.nbt.getTagList( MODULE_TAG, NBT.TAG_COMPOUND );
		final NBTTagCompound tarTag = mod.serializeNBT();
		modList.appendTag( tarTag );
		for( int i = modList.tagCount(); --i > idx; modList.set( i, modList.get( i - 1 ) ) );
		modList.set( idx, tarTag );
		
		// Update indices
		final int[] data = this.nbt.getIntArray( DATA_TAG );
		while( slot++ < this.indices.length )
		{
			final int val = 1 + this.getIdx( slot );
			this.setIdx( slot, val );
			this.setIdx( data, slot, val );
		}
	}
	
	@Override
	public T remove( int slot, int idx )
	{
		// Update installed list
		final int i = this.getIdx( slot ) + idx;
		final T removed = this.installed.remove( i );
		
		// Update NBT tag
		final NBTTagList modList = this.nbt.getTagList( MODULE_TAG, NBT.TAG_COMPOUND );
		modList.removeTag( i );
		
		// Update indices
		final int[] data = this.nbt.getIntArray( DATA_TAG );
		while( slot++ < this.indices.length )
		{
			final int val = -1 + this.getIdx( slot );
			this.setIdx( slot, val );
			this.setIdx( data, slot, val );
		}
		return removed;
	}
	
	@Override
	public T getInstalled( int slot, int idx ) {
		return this.installed.get( this.getIdx( slot ) + idx ); 
	}
	
	@Override
	public int getInstalledCount( int slot ) {
		return this.getIdx( slot + 1 ) - this.getIdx( slot );
	}
	
	@Override
	public int paintjob() { return this.paintjob; }
	
	@Override
	public void updatePaintjob( int paintjob )
	{
		this.paintjob = ( short ) paintjob;
		final int[] data = this.nbt.getIntArray( DATA_TAG );
		data[ 0 ] &= 0xFFFF;
		data[ 0 ] |= paintjob << 16;
	}
	
	@Override
	public IModifyState modifyState() { return this.modifyState; }
	
	@Override
	public void setModifyState( IModifyState state ) { this.modifyState = state; }
	
	@Override
	public NBTTagCompound serializeNBT() { return this.nbt; }
	
	@Override
	@SuppressWarnings( "unchecked" )
	public void deserializeNBT( NBTTagCompound nbt )
	{
		// Read paintjob
		final int data[] = nbt.getIntArray( DATA_TAG );
		this.paintjob = ( short ) ( data[ 0 ] >>> 16 );
		
		// Read install indices
		for( int i = this.indices.length; i > 0; --i )
			this.setIdx( i, this.getIdx( data, i ) );
		
		// Read installed modules
		this.installed.clear();
		final NBTTagList modList = nbt.getTagList( MODULE_TAG, NBT.TAG_COMPOUND );
		for( int i = 0, size = modList.tagCount(), slot = 0; i < size; ++i )
		{
			final NBTTagCompound modTag = modList.getCompoundTagAt( i );
			final IModular< ? > module = this.fromTag( modTag );
			
			while( i >= this.getIdx( slot + 1 ) ) ++slot;
			module.setBase( this, slot );
			this.installed.add( ( T ) module );
		}
		
		this.nbt = nbt; // Do not forget to bind to the given tag
	}
	
	protected abstract IModular< ? > onBeingRemoved();
	
	/**
	 * @return
	 *     Id that can be used to retrieve corresponding meta with the {@link #fromId(int)} of its
	 *     base. 16 bits valid in default implementation.
	 */
	protected abstract int id();
	
	protected abstract IModular< ? > fromTag( NBTTagCompound tag );
	
	protected int dataSize() { return 1 + ( this.indices.length + 3 ) / 4; }
	
	protected final int getIdx( int slot ) {
		return slot > 0 ? 0xFF & this.indices[ slot - 1 ] : 0;
	}
	
	protected final void setIdx( int slot, int idx ) {
		this.indices[ slot - 1 ] = ( byte ) idx;
	}
	
	// TODO: if 0 is never used then just remove it
	protected final int getIdx( int[] data, int slot )
	{
		final int islot = slot - 1;
		return slot > 0 ? 0xFF & data[ 1 + islot / 4 ] >>> ( islot % 4 ) * 8 : 0;
	}
	
	protected final void setIdx( int[] data, int slot, int val )
	{
		final int islot = slot - 1;
		final int i = 1 + islot / 4;
		final int offset = ( islot % 4 ) * 8;
		data[ i ] &= ~( 0xFF << offset );       // Clear value
		data[ i ] |= ( 0xFF & val ) << offset;  // Set value
	}
}
