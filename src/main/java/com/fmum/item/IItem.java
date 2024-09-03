package com.fmum.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

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
	
	/**
	 * @return Corresponding {@link ItemStack} of this item.
	 */
	ItemStack getBoundStack();
	
	default < T > Optional< T > lookupCapability( Capability< T > capability ) {
		return Optional.empty();
	}
	
	IEquippedItem onTakeOut( EnumHand hand, EntityPlayer player );
	
	
	/**
	 * @return
	 *     {@link Optional#empty()} if {@link ItemStack#getItem()} does not
	 *     implement {@link FMUMVanillaItem}. This usually corresponds to a
	 *     vanilla item.
	 */
	static Optional< IItem > ofOrEmpty( ItemStack stack )
	{
		final Item item = stack.getItem();
		if ( item instanceof FMUMVanillaItem )
		{
			final FMUMVanillaItem fi = ( FMUMVanillaItem ) item;
			return Optional.of( fi.getItemFrom( stack ) );
		}
		return Optional.empty();
	}
}
