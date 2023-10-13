package com.fmum.common.module;

import com.fmum.common.FMUM;
import com.fmum.util.Mat4f;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class Module< T extends IModule > implements IModule
{
	// TODO: Maybe make this configurable?
	protected static final String MODULE_TAG = "+";
	
	protected IModule base;
	protected int base_slot_idx;
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
	public IModule base() {
		return this.base;
	}
	
	@Override
	public int installationSlotIdx() {
		return this.base_slot_idx;
	}
	
	@Override
	public void _setBase( IModule base, int base_slot_idx )
	{
		this.base = base;
		this.base_slot_idx = base_slot_idx;
	}
	
	@Override
	public void _syncNBTTag() {
		this.base._syncNBTTag();
	}
	
	@Override
	public IModule _onBeingInstalled( IModule base, int base_slot_idx ) {
		return this;
	}
	
	@Override
	public void forEachInstalled( Consumer< ? super IModule > visitor ) {
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
	public IModule getInstalled( int slot_idx, int install_idx )
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
	public IModuleModifySession openModifySession() {
		return new ModifySession();
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
		for ( int i = 0, size = mod_list.tagCount(), slot = 0; i < size; i += 1 )
		{
			final NBTTagCompound mod_tag = mod_list.getCompoundTagAt( i );
			final IModule module = IModule.deserializeFrom( mod_tag );
			
			while ( i >= this._getSlotStartIdx( slot + 1 ) ) {
				slot += 1;
			}
			module._setBase( this, slot );
			this.installed_modules.add( ( T ) module );
		}
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
		final int s = slot_idx - 1;
		final int i = 1 + s / 4;
		final int offset = ( s % 4 ) * 8;
		data[ i ] &= ~( 0xFF << offset );       // Clear old value.
		data[ i ] |= ( 0xFF & val ) << offset;  // Set new value.
	}
	
	
	protected class ModifySession implements IModuleModifySession
	{
		protected final LinkedList< Runnable > modify_commands = new LinkedList<>();
		protected boolean is_valid_state = true;
		protected boolean can_preview = true;
		protected String cause = "";
		
		@Override
		public void setOffsetAndStep( int offset, int step ) { }
		
		@Override
		public void setPaintjob( int paintjob )
		{
			this.modify_commands.add( () -> {
				Module.this.paintjob = paintjob;
				final int[] data = Module.this.nbt.getIntArray( DATA_TAG );
				data[ 0 ] &= 0xFFFF;
				data[ 0 ] |= paintjob << 16;
			} );
		}
		
		@Override
		@SuppressWarnings( "unchecked" )
		public void install(
			int slot_idx,
			IModule module,
			Consumer< Integer > _out_install_idx
		) {
			this.modify_commands.add( () -> {
				final T mod = ( T ) module._onBeingInstalled( Module.this, slot_idx );
				
				// Update installed list.
				final int idx = Module.this._getSlotStartIdx( slot_idx + 1 );
				Module.this.installed_modules.add( idx, mod );
				
				// Update NBT tag.
				final NBTTagList mod_list = Module.this.nbt.getTagList( MODULE_TAG, NBT.TAG_COMPOUND );
				final NBTTagCompound mod_tag = mod.serializeNBT();
				mod_list.appendTag( mod_tag );
				for ( int i = mod_list.tagCount() - 1; i > idx; i -= 1 ) {
					mod_list.set( i, mod_list.get( i - 1 ) );
				}
				mod_list.set( idx, mod_tag );
				
				// Update indices.
				final int[] data = Module.this.nbt.getIntArray( DATA_TAG );
				for ( int i = slot_idx; i < Module.this.split_indices.length; i += 1 )
				{
					final int val = 1 + Module.this._getSlotStartIdx( i );
					Module.this._setSlotStartIdx( i, val );
					_setSlotStartIdxTo( data, i, val );
				}
				
				final int install_idx = idx - Module.this._getSlotStartIdx( slot_idx );
				_out_install_idx.accept( install_idx );
			} );
			
			final IModuleSlot slot = Module.this.getSlot( slot_idx );
			if ( !slot.isCompatibleWith( module ) )
			{
				this.is_valid_state = false;
				this.can_preview = false;
				this.cause += "Slot is not compatible with module.";
				return;
			}
			
			final int capacity = Math.min( FMUM.max_slot_capacity, slot.capacity() );
			if ( Module.this.getNumInstalledInSlot( slot_idx ) > capacity )
			{
				this.can_preview = false;
				this.cause += FMUM.MOD.format( "fmum.msg.exceed_max_slot_capacity", capacity );
				return;
			}
			
			// TODO: Check layer limit.
			// TODO: Post installation event.
		}
		
		@Override
		@SuppressWarnings( "unchecked" )
		public void remove(
			int slot_idx,
			int install_idx,
			Consumer< IModule > _out_removed_module
		) {
			// TODO: Post remove event.
			final int idx = Module.this._getSlotStartIdx( slot_idx ) + install_idx;
			final T raw_removed = Module.this.installed_modules.remove( idx );
			
			// Update NBT tag.
			final NBTTagList mod_list = Module.this.nbt.getTagList( MODULE_TAG, NBT.TAG_COMPOUND );
			mod_list.removeTag( idx );
			
			// Update indices.
			final int[] data = Module.this.nbt.getIntArray( DATA_TAG );
			final int size = Module.this.split_indices.length;
			for ( int i = slot_idx + 1; i <= size; i += 1 )
			{
				final int val = -1 + Module.this._getSlotStartIdx( i );
				Module.this._setSlotStartIdx( i, val );
				_setSlotStartIdxTo( data, i, val );
			}
			
			final T removed = ( T ) raw_removed._onBeingRemoved();
			_out_removed_module.accept( removed );
		}
		
		@Override
		public boolean isValidState() {
			return this.is_valid_state;
		}
		
		@Override
		public boolean canPreview() {
			return this.can_preview;
		}
		
		@Override
		public String cause() {
			return this.cause;
		}
		
		@Override
		public void commit()
		{
			this.modify_commands.forEach( Runnable::run );
			Module.this._syncNBTTag();
		}
	}
}
