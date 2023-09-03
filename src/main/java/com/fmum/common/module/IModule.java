package com.fmum.common.module;

import com.fmum.util.Category;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.function.Consumer;

public interface IModule< T extends IModule< ? extends T > >
	extends INBTSerializable< NBTTagCompound >
{
	String DATA_TAG = "*";
	
	String name();
	
	Category category();
	
	ItemStack boundenItemStack();
	
	IModule< ? > parent();
	
	int installationSlotIdx();
	
	/**
	 * Friend method that should only be used in between {@link IModule} instances.
	 */
	void _setParent( IModule< ? > parent, int installation_slot_idx );
	
	/**
	 * Force NBT change to be updated to the bounden target, so that the {@link Minecraft} will
	 * synchronize it to client side and save it on quit.
	 */
	void syncNBTTag();
	
	void forEachInstalled( Consumer< ? super T > visitor );
	
	int getNumInstalledInSlot( int slot_idx );
	
	T getInstalled( int slot_idx, int module_idx );
	
	default IModule< ? > getInstalled( byte[] idx_sequence, int sequence_len )
	{
		IModule< ? > mod = this;
		for ( int i = 0; i < sequence_len; i += 2 )
		{
			final int slot_idx = 0xFF & idx_sequence[ i ];
			final int module_idx = 0xFF & idx_sequence[ i + 1 ];
			mod = mod.getInstalled( slot_idx, module_idx );
		}
		return mod;
	}
	
	int slotCount();
	
	IModuleSlot getSlot( int idx );
	
	int offsetCount();
	
	int offset();
	
	int step();
	
	void modifyWith( Consumer< IModificationContext > modifier );
	
	/**
	 * <p> This is the standard method to get id from the any given module tag.
	 * Make sure your implementation is compatible with this method to guarantee
	 * your module will be deserialized correctly when it is installed onto some
	 * other modules that are not provided by you as they can not make a
	 * prediction of how to retrieve id from your module tag. </p>
	 *
	 * <p> For similar reason you should use this method to retrieve id from the
	 * data tag of the modules installed on your module to ensure compatibility.
	 * </p>
	 */
	static int getModuleID( NBTTagCompound tag ) {
		return 0xFFFF & tag.getIntArray( DATA_TAG )[ 0 ];
	}
	
	interface IModificationContext
	{
		void setOffsetAndStep( int offset, int step );
	}
}
