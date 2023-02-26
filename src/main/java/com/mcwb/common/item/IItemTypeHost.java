package com.mcwb.common.item;

import java.util.function.BiFunction;

import javax.annotation.Nullable;

import com.mcwb.common.meta.IMetaHost;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@FunctionalInterface
public interface IItemTypeHost extends IMetaHost
{
	@Override
	public IItemType meta();
	
	/**
	 * @see #getTypeOrDefault(ItemStack)
	 * @param stack Must be an non-empty stack and its item must be a valid type item
	 */
	public static IItemType getTypeA( ItemStack stack ) { return getType( stack.getItem() ); }
	
	/**
	 * Use {@link #getTypeA(ItemStack)} instead if you can guarantee that the stack is not empty and
	 * its item is a valid type item
	 * 
	 * @return {@link IItemType#VANILLA} if stack is empty or is not a valid type item
	 */
	public static IItemType getTypeOrDefault( ItemStack stack ) {
		return stack.isEmpty() ? IItemType.VANILLA : getTypeOrDefault( stack.getItem() );
	}
	
	/**
	 * @see #getTypeOrDefault(Item)
	 * @param item Must be a valid type item
	 */
	public static IItemType getType( Item item ) { return ( ( IItemTypeHost ) item ).meta(); }
	
	/**
	 * Use {@link #getType(Item)} instead if you can guarantee the item is valid type item
	 * 
	 * @return {@link IItemType#VANILLA} if the item is not a valid type item
	 */
	public static IItemType getTypeOrDefault( Item item ) {
		return item instanceof IItemTypeHost ? getType( item ) : IItemType.VANILLA;
	}
	
	/**
	 * Iterate through the given inventory
	 * 
	 * @param visitor Return something not {@code null} if should stop further iteration
	 * @return Value returned by visitor
	 */
	@Nullable
	public static < T > T streamInv(
		IInventory inv,
		BiFunction< IItemType, ItemStack, T > visitor
	) {
		for( int i = 0, size = inv.getSizeInventory(); i < size; ++i )
		{
			final ItemStack stack = inv.getStackInSlot( i );
			final T ret = visitor.apply( getTypeA( stack ), stack );
			if( ret != null ) return ret;
		}
		return null;
	}
}
