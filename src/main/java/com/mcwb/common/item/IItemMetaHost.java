package com.mcwb.common.item;

import java.util.function.BiFunction;

import javax.annotation.Nullable;

import com.mcwb.common.meta.IMetaHost;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;

import net.minecraft.item.ItemStack;

@FunctionalInterface
public interface IItemMetaHost extends IMetaHost
{
	@Override
	public IItemMeta meta();
	
	public static IItemMeta getMeta( ItemStack stack )
	{
		final Item item = stack.getItem();
		return item instanceof IItemMetaHost ? ( ( IItemMetaHost) item ).meta() : IItemMeta.VANILLA;
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
		BiFunction< IItemMeta, ItemStack, T > visitor
	) {
		for( int i = 0, size = inv.getSizeInventory(); i < size; ++i )
		{
			final ItemStack stack = inv.getStackInSlot( i );
			final T ret = visitor.apply( getMeta( stack ), stack );
			if( ret != null ) return ret;
		}
		return null;
	}
}
