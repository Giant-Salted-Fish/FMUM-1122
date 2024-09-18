package com.fmum.module;

import gsf.util.math.Mat4f;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.IntStream;

public abstract class Module implements IModule
{
	protected static final String MODULE_TAG = "+";
	
	
	protected IModule base = null;
	protected final Mat4f mat = new Mat4f();  // TODO: Re-check this.
	
	protected short paintjob_idx = 0;
	
	/**
	 * Installed modules have been flattened into a single list. The indices
	 * of the installed modules are stored in {@link #split_indices}.
	 */
	protected final ArrayList< IModule > installed_modules = new ArrayList<>();
	protected final byte[] split_indices = new byte[ this.getSlotCount() ];
	
	protected NBTTagCompound nbt;
	
	
	protected Module()
	{
		this.nbt = new NBTTagCompound();
		final int[] data = new int[ this._getDataArrLen() ];
		data[ 0 ] = 0xFFFF & this._getModuleID();
		this.nbt.setIntArray( DATA_TAG, data );
		this.nbt.setTag( MODULE_TAG, new NBTTagList() );
	}
	
	/**
	 * Unfortunately calling {@link #_deserializeAndBound(NBTTagCompound)} could
	 * cause error as it the fields of the subclass may not have been properly
	 * initialized. Hence, it needs to be delay after the constructor finishes
	 * its work.
	 *
	 * @param nbt To distinguish this from {@link #Module()}. Currently not used.
	 */
	protected Module( NBTTagCompound nbt ) {
		// Pass.
	}
	
	@Override
	public Optional< ? extends IModule > getBase() {
		return Optional.ofNullable( this.base );
	}
	
	@Override
	public void IModule$setBase( IModule base ) {
		this.base = base;
	}
	
	@Override
	public void IModule$clearBase() {
		this.base = null;
	}
	
	@Override
	public int countModuleInSlot( int slot_idx )
	{
		final int start_idx = this._getSlotStartIdx( slot_idx );
		final int end_idx = this._getSlotStartIdx( slot_idx + 1 );
		return end_idx - start_idx;
	}
	
	@Override
	public IModule getInstalled( int slot_idx, int module_idx )
	{
		final int idx = this._getSlotStartIdx( slot_idx ) + module_idx;
		return this.installed_modules.get( idx );
	}
	
	@Override
	public int getPaintjobIdx() {
		return this.paintjob_idx;
	}
	
	@Override
	public IModifyPreview< Integer > trySetPaintjob( int paintjob ) {
		return IModifyPreview.ok( () -> this._setPaintjob( paintjob ) );
	}
	
	protected int _setPaintjob( int paintjob )
	{
		this.paintjob_idx = ( short ) paintjob;
		final int[] data = this.nbt.getIntArray( DATA_TAG );
		data[ 0 ] &= 0xFFFF;
		data[ 0 ] |= paintjob << 16;
		return paintjob;
	}
	
	// Default implementation does not validate module to install.
	@Override
	public IModifyPreview< Integer > tryInstall( int slot_idx, IModule module ) {
		return IModifyPreview.ok( () -> this._install( slot_idx, module ) );
	}
	
	protected int _install( int slot_idx, IModule module )
	{
		final IModule mod = module;  // FIXME: On being installed.
		mod.IModule$setBase( this );
		
		// Update installed list.
		final int idx = this._getSlotStartIdx( slot_idx + 1 );
		this.installed_modules.add( idx, mod );
		
		// Update NBT tag.
		final NBTTagCompound nbt = this.nbt;
		final NBTTagList mod_lst = nbt.getTagList( MODULE_TAG, NBT.TAG_COMPOUND );
		final NBTTagCompound mod_tag = mod.getBoundNBT();
		mod_lst.appendTag( mod_tag );
		for ( int i = mod_lst.tagCount() - 1; i > idx; i -= 1 ) {
			mod_lst.set( i, mod_lst.get( i - 1 ) );
		}
		mod_lst.set( idx, mod_tag );
		
		// Update split indices.
		final int[] data = nbt.getIntArray( DATA_TAG );
		IntStream.rangeClosed( slot_idx + 1, this.split_indices.length )
			.forEach( slot -> {
				final int val = 1 + this._getSlotStartIdx( slot );
				this._setSlotStartIdx( slot, val );
				_setSlotStartIdx( data, slot, val );
			} );
		return idx - this._getSlotStartIdx( slot_idx );
	}
	
	@Override
	public IModifyPreview< IModule > tryRemove( int slot_idx, int module_idx ) {
		return IModifyPreview.ok( () -> this._remove( slot_idx, module_idx ) );
	}
	
	protected IModule _remove( int slot_idx, int module_idx )
	{
		// Update installed list.
		final int idx = _getSlotStartIdx( slot_idx ) + module_idx;
		final IModule removed = this.installed_modules.remove( idx );
		
		// Update NBT tag.
		final NBTTagCompound nbt = this.nbt;
		final NBTTagList mod_lst = nbt.getTagList( MODULE_TAG, NBT.TAG_COMPOUND );
		mod_lst.removeTag( idx );
		
		// Update split indices.
		final int[] data = nbt.getIntArray( DATA_TAG );
		IntStream.rangeClosed( slot_idx + 1, this.split_indices.length )
			.forEach( slot -> {
				final int val = -1 + this._getSlotStartIdx( slot );
				this._setSlotStartIdx( slot, val );
				_setSlotStartIdx( data, slot, val );
			} );
		
		removed.IModule$clearBase();
		return removed;  // FIXME: On being removed.
	}
	
	@Override
	public NBTTagCompound getBoundNBT() {
		return this.nbt;
	}
	
	protected void _deserializeAndBound( NBTTagCompound nbt )
	{
		// Bind to the NBT tag.
		this.nbt = nbt;
		
		// Read paintjob.
		final int[] data = nbt.getIntArray( DATA_TAG );
		this.paintjob_idx = ( short ) ( data[ 0 ] >>> 16 );
		
		// Read install indices.
		IntStream.rangeClosed( 1, this.split_indices.length )
			.forEach( i -> this._setSlotStartIdx( i, _getSlotStartIdx( data, i ) ) );
		
		// Read installed modules.
		this.installed_modules.clear();
		final NBTTagList mod_lst = nbt.getTagList( MODULE_TAG, NBT.TAG_COMPOUND );
		IntStream.range( 0, mod_lst.tagCount() )
			.mapToObj( mod_lst::getCompoundTagAt )
			.map( IModule::takeAndDeserialize )
			.forEachOrdered( mod -> {
				mod.IModule$setBase( this );
				this.installed_modules.add( mod );
			} );
	}
	
	protected abstract short _getModuleID();
	
	protected int _getDataArrLen()
	{
		final int split_arr_len = ( this.split_indices.length + 3 ) / 4;
		return 1 + split_arr_len;
	}
	
	protected final int _getSlotStartIdx( int slot_idx ) {
		return slot_idx > 0 ? 0xFF & this.split_indices[ slot_idx - 1 ] : 0;
	}
	
	protected final void _setSlotStartIdx( int slot_idx, int val ) {
		this.split_indices[ slot_idx - 1 ] = ( byte ) val;
	}
	
	protected static int _getSlotStartIdx( int[] data, int slot_idx )
	{
		final int slot = slot_idx - 1;
		final int i = 1 + ( slot / 4 );
		return slot_idx > 0 ? 0xFF & data[ i ] >>> ( slot % 4 ) * 8 : 0;
	}
	
	protected static void _setSlotStartIdx( int[] data, int slot_idx, int val )
	{
		final int slot = slot_idx - 1;
		final int i = 1 + ( slot / 4 );
		final int offset = ( slot % 4 ) * 8;
		data[ i ] &= ~( 0xFF << offset );       // Clear old value.
		data[ i ] |= ( 0xFF & val ) << offset;  // Set new value.
	}
}
