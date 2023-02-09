package com.mcwb.common.modify;

import java.util.ArrayList;
import java.util.function.Consumer;

import com.mcwb.client.IAutowirePlayerChat;
import com.mcwb.common.MCWB;

import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;

public abstract class Modifiable implements IModifiable, IAutowirePlayerChat
{
	protected static final String DATA_TAG = "d";
	protected static final String MODULE_TAG = "m";
	
	protected transient ModifyState modifyState = ModifyState.NOT_SELECTED;
	
	protected transient IModifiable base = ModuleWrapper.DEFAULT;
	protected transient short baseSlot = 0;
	
	protected short paintjob = 0;
	
	protected final ArrayList< IModifiable > installed = new ArrayList<>();
	protected final byte[] indices = new byte[ this.slotCount() ];
	
	/**
	 * Bounden NBT used for data persistence
	 */
	protected transient NBTTagCompound nbt;
	
	/**
	 * Create a new context that is not initialized. You must call
	 * {@link #deserializeNBT(NBTTagCompound)} to setup this context before you actual use it.
	 */
	protected Modifiable() { }
	
	/**
	 * Setup the given NBT tag and bind to it
	 * 
	 * @param nbtToBeInit A clean compound NBT that needed to be setup
	 */
	protected Modifiable( NBTTagCompound nbtToBeInit )
	{
		final int[] data = new int[ this.dataSize() ];
		data[ 0 ] = this.id();
		nbtToBeInit.setIntArray( DATA_TAG, data );
		nbtToBeInit.setTag( MODULE_TAG, new NBTTagList() );
		
		this.nbt = nbtToBeInit;
	}
	
	@Override
	public IModifiable base() { return this.base; }
	
	@Override
	public void setBase( IModifiable base, int baseSlot )
	{
		this.base = base;
		this.baseSlot = ( short ) baseSlot;
		
		// Update position for server side. Client side will always update matrix before render.
		// FIXME: matrix is not serialized hence this will not work on server side
//		this.globalMat.setIdentity();
//		base.applySlotTransform( this, this.globalMat );
	}
	
	@Override
	public void forEach( Consumer< IModifiable > visitor )
	{
		visitor.accept( this );
		this.installed.forEach( mod -> mod.forEach( visitor ) );
	}
	
	@Override
	public void install( int slot, IModifiable module )
	{
		module.onBeingInstalled( this, slot );
		final int idx = this.getIdx( slot + 1 );
		
		// Update installed list
		this.installed.add( idx, module );
		
		// Update NBT tag
		final NBTTagList modList = this.nbt.getTagList( MODULE_TAG, NBT.TAG_COMPOUND );
		final NBTTagCompound tarTag = module.serializeNBT();
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
		this.syncNBTData();
	}
	
	@Override
	public ModifyPredication tryInstallPreview( int islot, IModifiable module )
	{
		final IModuleSlot slot = this.getSlot( islot );
		if( !slot.isAllowed( module ) )
			return ModifyPredication.NO_PREVIEW;
		
		final int capacity = Math.min( MCWB.maxSlotCapacity, slot.capacity() );
		if( this.getInstalledCount( islot ) >= capacity )
		{
			return ( ModifyPredication.NotOk ) () -> this.sendPlayerPrompt(
				I18n.format( "mcwb.msg.arrive_max_module_capacity", capacity )
			);
		}
		
		this.install( islot, module );
		return ModifyPredication.OK;
	}
	
	@Override
	public ModifyPredication checkInstalledPosition( IModifiable installed )
	{
		// TODO: check hitbox
		return ModifyPredication.OK;
	}
	
	@Override
	public IModifiable remove( int slot, int idx )
	{
		// Update installed list
		final int tarIdx = this.getIdx( slot ) + idx;
		final IModifiable target = this.installed.remove( tarIdx );
		
		// Update nbt tag
		final NBTTagList modList = this.nbt.getTagList( MODULE_TAG, NBT.TAG_COMPOUND );
		modList.removeTag( tarIdx );
		
		// Update indices
		final int[] data = this.nbt.getIntArray( DATA_TAG );
		while( slot++ < this.indices.length )
		{
			final int val = -1 + this.getIdx( slot );
			this.setIdx( slot, val );
			this.setIdx( data, slot, val );
		}
		
		final IModifiable ret = target.onBeingRemoved();
		this.syncNBTData();
		return ret;
	}
	
	@Override
	public IModifiable onBeingRemoved()
	{
		this.base = ModuleWrapper.DEFAULT;
		this.baseSlot = 0;
		return this;
		
		// TODO: Update position for server side
//		this.globalMat.setIdentity();
	}
	
	@Override
	public IModifiable getInstalled( int slot, int idx ) {
		return this.installed.get( this.getIdx( slot ) + idx );
	}
	
	@Override
	public int getInstalledCount( int slot ) {
		return this.getIdx( slot + 1 ) - this.getIdx( slot );
	}
	
	@Override
	public int paintjob() { return this.paintjob; }
	
	@Override
	public void $paintjob( int paintjob )
	{
		this.paintjob = ( short ) paintjob;
		final int[] data = this.nbt.getIntArray( DATA_TAG );
		data[ 0 ] &= 0xFFFF;
		data[ 0 ] |= paintjob << 16;
		this.syncNBTData();
	}
	
	@Override
	public ModifyState modifyState() { return this.modifyState; }
	
	@Override
	public void $modifyState( ModifyState state ) { this.modifyState = state; }
	
	@Override
	public NBTTagCompound serializeNBT() { return this.nbt; }
	
	@Override
	public void deserializeNBT( NBTTagCompound nbt )
	{
		// Read paintjob
		final int data[] = nbt.getIntArray( DATA_TAG );
		this.paintjob = ( short ) ( data[ 0 ] >>> 16 );
		
		// Read install indices
		for( int i = this.indices.length; i > 0; --i )
			this.setIdx( i, this.getIdx( data, i ) );
		
		// Read installed modules
		this.installed.clear(); // TODO: check if this is needed?
		final NBTTagList modList = nbt.getTagList( MODULE_TAG, NBT.TAG_COMPOUND );
		for( int i = 0, size = modList.tagCount(), slot = 0; i < size; ++i )
		{
			final NBTTagCompound modTag = modList.getCompoundTagAt( i );
			final int modId = 0xFFFF & modTag.getIntArray( DATA_TAG )[ 0 ];
			final IModifiableType type = this.fromId( modId );
			final IModifiable module = type.deserializeContexted( modTag );
			
			while( i >= this.getIdx( slot + 1 ) ) ++slot;
			module.setBase( this, slot );
			this.installed.add( module );
		}
		
		this.nbt = nbt; // Do not forget to bind to the given tag
	}
	
	/**
	 * @return
	 *     Id that can be used to retrieve corresponding meta with the {@link #fromId(int)} of its
	 *     base. 16 bits valid in default implementation.
	 */
	protected abstract int id();
	
	protected abstract IModifiableType fromId( int id );
	
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
	
	protected int dataSize() { return 1 + ( this.indices.length + 3 ) / 4; }
}
