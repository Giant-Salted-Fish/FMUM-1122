package com.mcwb.common.module;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.google.common.base.Supplier;
import com.mcwb.common.MCWB;
import com.mcwb.common.module.IModuleEventSubscriber.ModuleInstallEvent;
import com.mcwb.common.module.IModuleEventSubscriber.ModuleRemoveEvent;
import com.mcwb.common.paintjob.IPaintable;
import com.mcwb.util.Mat4f;

import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;

public abstract class Module< T extends IModular< ? extends T > >
	implements IModular< T >, IPaintable
{
	protected static final String MODULE_TAG = "m";
	
	protected transient final Mat4f mat = new Mat4f();
	
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
	public IModular< ? > base() { return this.base; }
	
	@Override
	public void setBase( IModular< ? > base, int baseSlot )
	{
		this.base = base;
		this.baseSlot = ( short ) baseSlot;
	}
	
	@Override
	public void postEvent( Object evt ) { this.base.postEvent( evt ); }
	
	@Override
	public void syncAndUpdate() { this.base.syncAndUpdate(); }
	
	@Override
	public void updateState( BiConsumer< Class< ? >, IModuleEventSubscriber< ? > > registry )
	{
		this.mat.setIdentity();
		this.base.applyTransform( this.baseSlot, this, this.mat );
		
		this.installed.forEach( mod -> mod.updateState( registry ) );
	}
	
	@Override
	public IModifyPredicate tryInstall( int islot, IModular< ? > module )
	{
		final IModuleSlot slot = this.getSlot( islot );
		if( !slot.isAllowed( module ) ) return IModifyPredicate.NO_PREVIEW;
		
		final int capacity = Math.min( MCWB.maxSlotCapacity, slot.capacity() );
		if( this.getInstalledCount( islot ) > capacity )
		{
			final String msg = "mcwb.msg.arrive_max_module_capacity";
			return ( IModifyPredicate.NotOk ) () -> I18n.format( msg, capacity );
		}
		
		final Supplier< IModifyPredicate > action = () -> {
			final int idx = this.base.install( islot, module );
			return () -> this.base.getInstalled( islot, idx );
		};
		final ModuleInstallEvent evt = new ModuleInstallEvent( this, islot, module, action );
		this.postEvent( evt );
		return evt.action.get();
	}
	
	@Override
	public IModular< ? > doRemove( int slot, int idx )
	{
		final Supplier< IModular< ? > > action = () -> this.base.remove( slot, idx );
		final ModuleRemoveEvent evt = new ModuleRemoveEvent( this, slot, idx, action );
		this.postEvent( evt );
		return evt.action.get();
	}
	
	@Override
	@SuppressWarnings( "unchecked" )
	public int install( int slot, IModular< ? > module )
	{
		final T mod = ( T ) module.onBeingInstalled();
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
		this.syncAndUpdate();
		return idx;
	}
	
	@Override
	public IModular< ? > remove( int slot, int idx )
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
		this.syncAndUpdate();
		return removed.onBeingRemoved();
	}
	
	@Override
	public IModular< ? > onBeingInstalled() { return this; }
	
	@Override
	public IModular< ? > onBeingRemoved()
	{
		final IModular< ? > wrapper = this.wrapOnBeingRemoved();
		wrapper.syncAndUpdate();
		return wrapper;
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
	public int getInstalledCount( int slot ) {
		return this.getIdx( slot + 1 ) - this.getIdx( slot );
	}
	
	@Override
	public T getInstalled( int slot, int idx ) {
		return this.installed.get( this.getIdx( slot ) + idx ); 
	}
	
	@Override
	public int paintjob() { return this.paintjob; }
	
	@Override
	public void setPaintjob( int paintjob )
	{
		this.paintjob = ( short ) paintjob;
		final int[] data = this.nbt.getIntArray( DATA_TAG );
		data[ 0 ] &= 0xFFFF;
		data[ 0 ] |= paintjob << 16;
		this.syncAndUpdate();
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
	
	protected int dataSize() { return 1 + ( this.indices.length + 3 ) / 4; }
	
	/**
	 * @return
	 *     Id that can be used to retrieve corresponding meta with the {@link #fromId(int)} of its
	 *     base. 16 bits valid in default implementation.
	 */
	protected abstract int id();
	
	/**
	 * Used in {@link #onBeingRemoved()} to wrap this module on being removed.
	 * {@link #syncAndUpdate()} will be called on returned wrapper.
	 */
	protected abstract IModular< ? > wrapOnBeingRemoved();
	
	protected abstract IModular< ? > fromTag( NBTTagCompound tag );
	
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
