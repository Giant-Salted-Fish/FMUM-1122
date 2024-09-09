package com.fmum.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 * This corresponds to the concept of {@link ItemStack} in Minecraft.
 */
public interface IItem
{
	/**
	 * It is recommended to use this retrieve {@link IItem} instance from the
	 * capability system of the {@link ItemStack} to keep maximum compatibility.
	 */
	@CapabilityInject( IItem.class )
	Capability< IItem > CAPABILITY = null;
	
	
	/**
	 * Currently this API does not guarantee the returned {@link IItemType} is
	 * always the same! Always to retrieve the type before you use!
	 */
	IItemType getType();
	
	default < T > Optional< T > lookupCapability( Capability< T > capability ) {
		return Optional.empty();
	}
	
	IEquippedItem onTakeOut( EnumHand hand, EntityPlayer player );
	
	
	@SuppressWarnings( "DataFlowIssue" )
	static Optional< IItem > ofOrEmpty( ItemStack stack ) {
		return Optional.ofNullable( stack.getCapability( CAPABILITY, null ) );
	}
	
	static ICapabilityProvider newProviderOf( IItem item )
	{
		return new ICapabilityProvider() {
			@Override
			@SuppressWarnings( "ConstantValue" )
			public boolean hasCapability( @Nonnull Capability< ? > capability, @Nullable EnumFacing facing ) {
				return capability == CAPABILITY;
			}
			
			@Nullable
			@Override
			@SuppressWarnings( "ConstantValue" )
			public < T > T getCapability( @Nonnull Capability< T > capability, @Nullable EnumFacing facing ) {
				return capability == CAPABILITY ? CAPABILITY.cast( item ) : null;
			}
		};
	}
}
