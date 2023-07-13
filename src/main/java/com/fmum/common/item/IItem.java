package com.fmum.common.item;

import com.fmum.common.player.PlayerPatch;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public interface IItem
{
	@CapabilityInject( IItem.class )
	Capability< IItem > CAPABILITY = null;
	
	IItem VANILLA = new IItem()
	{
		@Override
		public int stackId() { return 0; }
		
		@Override
		public IEquippedItem< ? > onTakeOut( EntityPlayer player, EnumHand hand ) {
			return null;
		}
	};
	
	/**
	 * Used by {@link PlayerPatch} to determine whether the item in hand has changed or not.
	 */
	int stackId();
	
	IEquippedItem< ? > onTakeOut( EntityPlayer player, EnumHand hand );
	
	/**
	 * This will crash if the item of given stack is not of type {@link IFMUMVanillaItem}.
	 *
	 * @see #getFromOrDefault(ItemStack)
	 */
	static IItem getFrom( ItemStack stack )
	{
		final IItemType type = IItemType.getFrom( stack.getItem() );
		return type.getItem( stack );
	}
	
	/**
	 * @see #getFrom(ItemStack)
	 * @return {@link #VANILLA} if the item of given stack is not of type {@link IFMUMVanillaItem}.
	 */
	static IItem getFromOrDefault( ItemStack stack )
	{
		final IItemType type = IItemType.getFromOrDefault( stack.getItem() );
		return type.getItem( stack );
	}
}
