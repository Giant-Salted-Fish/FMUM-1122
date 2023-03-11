package com.mcwb.common.item;

import com.mcwb.common.meta.IContexted;

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
public interface IItem extends IContexted
{
	public static final IItem EMPTY = new IItem()
	{
		@Override
		public IEquippedItem onTakeOut( IEquippedItem oldItem, EntityPlayer player, EnumHand hand ) {
			return IEquippedItem.EMPTY;
		}
		
		@Override
		public IEquippedItem onInHandStackChanged(
			IEquippedItem oldItem,
			EntityPlayer player,
			EnumHand hand
		) { return IEquippedItem.EMPTY; }
		
		@Override
		@SideOnly( Side.CLIENT )
		public ResourceLocation texture() { return null; }
	};
	
	public static final IItem VANILLA = new IItem()
	{
		@Override
		public IEquippedItem onTakeOut( IEquippedItem oldItem, EntityPlayer player, EnumHand hand ) {
			return IEquippedItem.VANILLA;
		}
		
		@Override
		public IEquippedItem onInHandStackChanged(
			IEquippedItem oldItem,
			EntityPlayer player,
			EnumHand hand
		) { return IEquippedItem.VANILLA; }
		
		@Override
		@SideOnly( Side.CLIENT )
		public ResourceLocation texture() { return null; }
	};
	
	public IEquippedItem onTakeOut( IEquippedItem oldItem, EntityPlayer player, EnumHand hand );
	
	public IEquippedItem onInHandStackChanged(
		IEquippedItem oldItem,
		EntityPlayer player,
		EnumHand hand
	);
	
	@SideOnly( Side.CLIENT )
	public ResourceLocation texture(); // TODO: check if this is used
}
