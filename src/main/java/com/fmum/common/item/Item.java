package com.fmum.common.item;

import com.fmum.common.player.PlayerPatch;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public interface Item
{
	@CapabilityInject( Item.class )
	Capability< Item > CAPABILITY = null;
	
	Item VANILLA = new Item()
	{
		@Override
		public int stackId() { return 0; }
		
		@Override
		public EquippedItem< ? > onTakeOut(
			EntityPlayer player, EnumHand hand
		) { return null; }
	};
	
	/**
	 * Used by {@link PlayerPatch} to determine whether the item in hand has changed or not.
	 */
	int stackId();
	
	EquippedItem< ? > onTakeOut( EntityPlayer player, EnumHand hand );
	
	/**
	 * This will crash if the item of given stack is not of type {@link FMUMVanillaItem}.
	 *
	 * @see #getFromOrDefault(ItemStack)
	 */
	static Item getFrom( ItemStack stack )
	{
		final ItemType type = ItemType.getFrom( stack.getItem() );
		return type.getItem( stack );
	}
	
	/**
	 * @see #getFrom(ItemStack)
	 * @return {@link #VANILLA} if the item of given stack is not of type {@link FMUMVanillaItem}.
	 */
	static Item getFromOrDefault( ItemStack stack )
	{
		final ItemType type = ItemType.getFromOrDefault( stack.getItem() );
		return type.getItem( stack );
	}
}
