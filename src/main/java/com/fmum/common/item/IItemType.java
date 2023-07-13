package com.fmum.common.item;

import com.fmum.common.Registry;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface IItemType
{
	Registry< IItemType > REGISTRY = new Registry<>( IItemType::name );
	
	IItemType VANILLA = new IItemType()
	{
		@Override
		public String name() { return "vanilla"; }
		
		@Override
		public Item vanillaItem() { return Items.AIR; }
		
		@Override
		public IItem getItem( ItemStack stack ) { return IItem.VANILLA; }
	};
	
	String name();
	
	Item vanillaItem();
	
	IItem getItem( ItemStack stack );
	
	/**
	 * Given item must be an instance of {@link IFMUMVanillaItem}.
	 *
	 * @see #getFromOrDefault(Item)
	 */
	static IItemType getFrom( Item vanilla_item ) {
		return ( ( IFMUMVanillaItem ) vanilla_item ).type();
	}
	
	/**
	 * @see #getFrom(Item)
	 * @return {@link #VANILLA} if given item is not an instance of {@link IFMUMVanillaItem}.
	 */
	static IItemType getFromOrDefault( Item vanilla_item )
	{
		final boolean is_fmum_item = vanilla_item instanceof IFMUMVanillaItem;
		return is_fmum_item ? getFrom( vanilla_item ) : VANILLA;
	}
}
