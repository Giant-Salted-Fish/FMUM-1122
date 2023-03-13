package com.mcwb.common.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Represents {@link IItemType} with context
 * 
 * @author Giant_Salted_Fish
 */
public interface IItem
{
	public static final IItem VANILLA = new IItem()
	{
		@Override
		public int stackId() { return 0; }
		
		@Override
		public IEquippedItem< ? > onTakeOut( EntityPlayer player, EnumHand hand ) {
			return IEquippedItem.VANILLA;
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public ResourceLocation texture() { return null; }
	};
	
	/**
	 * This will be used to determinate whether the item in hand has changed
	 * 
	 * @return An universe id that identifies an item stack
	 */
	public int stackId();
	
	/**
	 * Called when player is trying to take out this item
	 */
	public IEquippedItem< ? > onTakeOut( EntityPlayer player, EnumHand hand );
	
	@SideOnly( Side.CLIENT )
	public ResourceLocation texture();
}
