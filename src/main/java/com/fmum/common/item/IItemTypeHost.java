package com.fmum.common.item;

import com.fmum.common.meta.IMetaHost;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface IItemTypeHost extends IMetaHost
{
	@Override
	IItemType meta();
	
	/**
	 * Use {@link #getItem(ItemStack)} instead of this if you can guarantee the stack has a valid
	 * type item.
	 * 
	 * @return {@link IItem#VANILLA} if stack does not have a valid type host item.
	 */
	static IItem getItemOrDefault( ItemStack stack ) {
		return getTypeOrDefault( stack.getItem() ).getContexted( stack );
	}
	
	/**
	 * @see #getItemOrDefault(ItemStack)
	 * @param stack Must have a valid type host item.
	 */
	static IItem getItem( ItemStack stack ) {
		return getType( stack.getItem() ).getContexted( stack );
	}
	
	/**
	 * Use {@link #getType(Item)} instead of this if you can guarantee the item is valid type host.
	 * 
	 * @return {@link IItemType#VANILLA} if the item is not a valid type host.
	 */
	static IItemType getTypeOrDefault( Item item ) {
		return item instanceof IItemTypeHost ? getType( item ) : IItemType.VANILLA;
	}
	
	/**
	 * @see #getTypeOrDefault(Item)
	 * @param item Must be a valid type host.
	 */
	static IItemType getType( Item item ) { return ( ( IItemTypeHost) item ).meta(); }
}
