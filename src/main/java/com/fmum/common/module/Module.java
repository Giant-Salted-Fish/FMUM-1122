package com.fmum.common.module;

import com.fmum.util.Mat4f;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract  class Module< T extends IModule< ? extends T > > implements IModule< T >
{
	protected static final String MODULE_TAG = "+";
	
	protected IModule< ? > parent;
	protected int installation_slot_idx;
	protected final Mat4f mat = new Mat4f();
	
	protected int paintjob_idx;
	
	protected final ArrayList< T > installed_modules = new ArrayList<>();
	protected final byte[] split_indices = new byte[ this.totalSlotCount() ];
	
	protected NBTTagCompound nbt;
	
	protected Module()
	{
		this.nbt = new NBTTagCompound();
		final int[] data = new int[ this._dataArrSize() ];
		data[ 0 ] = this._id();
		this.nbt.setIntArray( DATA_TAG, data );
		this.nbt.setTag( MODULE_TAG, new NBTTagList() );
	}
	
	/**
	 * Unfortunately calling {@link #deserializeNBT(NBTBase)} could cause error as it the fields of
	 * the subclass may not have been properly initialized. Hence, it needs to be delay after the
	 * constructor finishes its work.
	 *
	 * @param nbt To distinguish this from {@link #Module()}. Currently not used.
	 */
	protected Module( NBTTagCompound nbt ) { }
	
	@Override
	public ItemStack boundenItemStack() {
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
	public void forEachInstalled( Consumer< ? super T > visitor ) {
		this.installed_modules.forEach( visitor );
	}
	
	@Override
	public int getNumInstalledInSlot( int slot_idx ) {
		return this._getSlotStartIdx( slot_idx + 1 ) - this._getSlotStartIdx( slot_idx );
	}
	
	@Override
	public T getInstalled( int slot_idx, int module_idx )
	{
		final int idx = this._getSlotStartIdx( slot_idx ) + module_idx;
		return this.installed_modules.get( idx );
	}
	
	/**
	 * @return 16-bits valid in default implementation.
	 */
	protected abstract int _id();
	
	protected int _dataArrSize() {
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
