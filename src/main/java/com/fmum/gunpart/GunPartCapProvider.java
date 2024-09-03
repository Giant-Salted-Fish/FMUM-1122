package com.fmum.gunpart;

import com.fmum.gunpart.GunPartType.GunPart;
import com.fmum.item.IItem;
import com.fmum.module.IModule;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GunPartCapProvider implements ICapabilitySerializable< NBTTagCompound >
{
	@CapabilityInject( GunPartCapProvider.class )
	public static final Capability< GunPartCapProvider > CAPABILITY = null;
	
	
	protected final ItemStack stack;
	
	protected NBTTagCompound nbt = null;
	protected IItem item = null;
	
	protected GunPartCapProvider( ItemStack stack ) {
		this.stack = stack;
	}
	
	@Override
	@SuppressWarnings( "ConstantValue" )
	public boolean hasCapability( @Nonnull Capability< ? > capability, @Nullable EnumFacing facing ) {
		return ( capability == IItem.CAPABILITY && this.nbt != null ) || capability == CAPABILITY;
	}
	
	@Nullable
	@Override
	@SuppressWarnings( "ConstantValue" )
	public < T > T getCapability( @Nonnull Capability< T > capability, @Nullable EnumFacing facing )
	{
		if ( capability != IItem.CAPABILITY )
		{
			final boolean is_self_cap = capability == CAPABILITY;
			return is_self_cap ? CAPABILITY.cast( this ) : null;
		}
		
		// Lazy init to avoid overhead for intermediate ItemStack copy use.
		if ( this.item != null ) {
			return IItem.CAPABILITY.cast( this.item );
		}
		
		// This is always true if the stack is created with {new ItemStack(...)}.
		if ( this.nbt == null ) {
			return null;
		}
		
		final IModule module = IModule.takeAndDeserialize( this.nbt );
		final GunPart gun_part = ( GunPart ) module;
		this.item = gun_part._createItem( this.stack );
		return IItem.CAPABILITY.cast( this.item );
	}
	
	@Override
	public NBTTagCompound serializeNBT()
	{
		// Copying before return is necessary, because ItemStack#copy() will \
		// directly use and bound to the returned NBT tag.
		return this.nbt != null ? this.nbt.copy() : new NBTTagCompound();
	}
	
	@Override
	public void deserializeNBT( NBTTagCompound nbt )
	{
		this.nbt = nbt;
		this.item = null;
	}
}
