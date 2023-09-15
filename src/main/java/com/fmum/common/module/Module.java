package com.fmum.common.module;

import com.fmum.util.Mat4f;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class Module< T extends IModule< ? extends T > >
	implements IModule< T >
{
	// TODO: Maybe make this configurable?
	protected static final String MODULE_TAG = "+";
	
	protected IModule< ? > parent;
	protected int installation_slot_idx;
	protected final Mat4f mat = new Mat4f();
	
	protected int paintjob;
	
	protected final ArrayList< T > installed_modules = new ArrayList<>();
	protected final byte[] split_indices = new byte[ this.slotCount() ];
	
	protected NBTTagCompound nbt;
	
	protected Module()
	{
		this.nbt = new NBTTagCompound();
		final int[] data = new int[ this._dataSize() ];
		data[ 0 ] = this._id();
		this.nbt.setIntArray( DATA_TAG, data );
		this.nbt.setTag( MODULE_TAG, new NBTTagList() );
	}
	
	/**
	 * Unfortunately calling {@link #deserializeNBT(NBTTagCompound)} could cause
	 * error as it the fields of the subclass may not have been properly
	 * initialized. Hence, it needs to be delay after the constructor finishes
	 * its work.
	 *
	 * @param nbt To distinguish this from {@link #Module()}. Currently not used.
	 */
	protected Module( NBTTagCompound nbt ) { }
	
	@Override
	public ItemStack itemStack() {
		throw new RuntimeException();
	}
	
	@Override
	public IModule< ? > parent() {
		return this.parent;
	}
	
	@Override
	public int installationSlotIdx() {
		return this.installation_slot_idx;
	}
	
	@Override
	public void _setParent( IModule< ? > parent, int installation_slot_idx )
	{
		this.parent = parent;
		this.installation_slot_idx = installation_slot_idx;
	}
	
	@Override
	public void _syncNBTTag() {
		this.parent._syncNBTTag();
	}
	
	@Override
	public void forEachInstalled( Consumer< ? super T > visitor ) {
		this.installed_modules.forEach( visitor );
	}
	
	@Override
	public int getNumInstalledInSlot( int slot_idx )
	{
		final int start_idx = this._getSlotStartIdx( slot_idx );
		final int end_idx = this._getSlotStartIdx( slot_idx + 1 );
		return end_idx - start_idx;
	}
	
	@Override
	public T getInstalled( int slot_idx, int install_idx )
	{
		final int idx = this._getSlotStartIdx( slot_idx ) + install_idx;
		return this.installed_modules.get( idx );
	}
	
	@Override
	public int paintjob() {
		return this.paintjob;
	}
	
	@Override
	public Optional< Runnable > testAffordable( EntityPlayer player )
	{
		// TODO: Test affordability.
		if ( player.capabilities.isCreativeMode )
		{
			return Optional.of( () -> { } );
		}
		
		return Optional.empty();
	}
	
	@Override
	public NBTTagCompound serializeNBT() {
		return this.nbt;
	}
	
	@Override
	@SuppressWarnings( "unchecked" )
	public void deserializeNBT( NBTTagCompound nbt )
	{
		// Bind to given NBT tag.
		this.nbt = nbt;
		
		// Read paintjob.
		final int[] data = nbt.getIntArray( DATA_TAG );
		this.paintjob = ( short ) ( data[ 0 ] >>> 16 );
		
		// Read install indices.
		for ( int i = this.split_indices.length; i > 0; i -= 1 ) {
			this._setSlotStartIdx( i, _getSlotStartIdxFrom( data, i ) );
		}
		
		// Read installed modules.
		this.installed_modules.clear();
		final NBTTagList mod_list = nbt
			.getTagList( MODULE_TAG, NBT.TAG_COMPOUND );
		for ( int i = 0, size = mod_list.tagCount(), slot = 0; i < size; i += 1)
		{
			final NBTTagCompound mod_tag = mod_list.getCompoundTagAt( i );
			final IModule< ? > module = IModule.deserializeFrom( mod_tag );
			
			while ( i >= this._getSlotStartIdx( slot + 1 ) ) {
				slot += 1;
			}
			module._setParent( this, slot );
			this.installed_modules.add( ( T ) module );
		}
	}
	
	protected void _setPaintjob( int paintjob )
	{
		this.paintjob = paintjob;
		final int[] data = this.nbt.getIntArray( DATA_TAG );
		data[ 0 ] &= 0xFFFF;
		data[ 0 ] |= paintjob << 16;
	}
	
	/**
	 * @return 16-bits valid in default implementation.
	 */
	protected abstract int _id();
	
	protected int _dataSize() {
		return 1 + ( this.split_indices.length + 3 ) / 4;
	}
	
	protected final int _getSlotStartIdx( int slot_idx ) {
		return slot_idx > 0 ? 0xFF & this.split_indices[ slot_idx - 1 ] : 0;
	}
	
	protected final void _setSlotStartIdx( int slot_idx, int val ) {
		this.split_indices[ slot_idx - 1 ] = ( byte ) val;
	}
	
	protected static int _getSlotStartIdxFrom( int[] data, int slot_idx )
	{
		final int s = slot_idx - 1;
		return slot_idx > 0 ? 0xFF & data[ 1 + s / 4 ] >>> ( s % 4 ) * 8 : 0;
	}
	
	protected static void _setSlotStartIdxTo( int[] data, int slot_idx, int val )
	{
		final int s = slot_idx -= 1;
		final int i = 1 + s / 4;
		final int offset = ( s % 4 ) * 8;
		data[ i ] &= ~( 0xFF << offset );       // Clear old value.
		data[ i ] |= ( 0xFF & val ) << offset;  // Set new value.
	}
}
