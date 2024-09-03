package com.fmum.module;

import com.fmum.item.ItemCategory;
import com.mojang.realmsclient.util.Pair;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;
import java.util.function.Supplier;

public interface IModule
{
	@CapabilityInject( IModule.class )
	Capability< IModule > CAPABILITY = null;
	
	String DATA_TAG = "*";
	
	
	ItemCategory getCategory();
	
	/**
	 * @return The module that this module installed on.
	 */
	Optional< ? extends IModule > getBase();
	
	/**
	 * Friend method that should only be used in between {@link IModule} instances.
	 */
	void IModule$setBase( IModule base );
	
	void IModule$clearBase();  // TODO: Maybe do this in on being removed?
	
	int countModuleInSlot( int slot_idx );
	
	IModule getInstalled( int slot_idx, int module_idx );
	
	int getSlotCount();
	
	int getPaintjobCount();
	
	int getPaintjobIdx();
	
	IModifyPreview< Integer > trySetPaintjob( int paintjob );
	
	IModifyPreview< Integer > tryInstall( int slot_idx, IModule module );
	
	IModifyPreview< ? extends IModule > tryRemove( int slot_idx, int module_idx );
	
	@SideOnly( Side.CLIENT )
	Pair< ? extends IModule, Supplier< ? extends IModule > > getModifyCursor(
		int slot_idx,
		int module_idx,
		IModifyContext ctx
	);
	
	@SideOnly( Side.CLIENT )
	int installPreviewPlaceholder( int slot_idx, IModifyContext ctx );
	
	/**
	 * You should not modify the returned NBT on yourself. The returned NBT is
	 * highly likely bound to the module and is directly returned due to the
	 * performance consideration. Changes to the returned NBT may lead to
	 * unexpected behaviors. If you really need, copy before you modify the
	 * returned tag.
	 */
	NBTTagCompound getBoundNBT();
	
	ItemStack takeAndToStack();
	
	/**
	 * <p> This is the standard method to get id from the any given module tag.
	 * Make sure your implementation is compatible with this method to guarantee
	 * your module will be deserialized correctly when it is installed onto some
	 * other modules that are not provided by you as they can not make a
	 * prediction of how to retrieve id from your module tag. </p>
	 *
	 * <p> For similar reason you should use this method to retrieve id from the
	 * data tag of the modules installed on your module to ensure compatibility. </p>
	 *
	 * @see #takeAndDeserialize(NBTTagCompound)
	 */
	static short getModuleID( NBTTagCompound nbt ) {
		return ( short ) nbt.getIntArray( DATA_TAG )[ 0 ];
	}
	
	/**
	 * Uses the id retrieved by {@link #getModuleID(NBTTagCompound)} to get
	 * module type from {@link IModuleType#REGISTRY} and deserialize the module.
	 */
	static IModule takeAndDeserialize( NBTTagCompound nbt )  // TODO: Make it optional?
	{
		final short module_id = getModuleID( nbt );
		return (
			IModuleType.REGISTRY.lookup( module_id )
			.map( type -> type.takeAndDeserialize( nbt ) )
			.orElseThrow( () -> {
				final String err_msg = "Invalid module id: " + module_id;
				return new IllegalArgumentException( err_msg );
			} )
		);
	}
	
	static Optional< IModule > tryGetInstalled( IModule mod, byte[] loc, int len )
	{
		for ( int i = 0; i < len; i += 2 )
		{
			final int slot_idx = 0xFF & loc[ i ];
			final int slot_count = mod.getSlotCount();
			if ( slot_idx >= slot_count ) {
				return Optional.empty();
			}
			
			final int module_idx = 0xFF & loc[ i + 1 ];
			final int mod_count = mod.countModuleInSlot( slot_idx );
			if ( module_idx >= mod_count ) {
				return Optional.empty();
			}
			
			mod = mod.getInstalled( slot_idx, module_idx );
		}
		return Optional.of( mod );
	}
}
