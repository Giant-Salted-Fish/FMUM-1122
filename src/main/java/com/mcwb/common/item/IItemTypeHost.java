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
	
	/***
	 * @return {@link IItemType#VANILLA} if stack is empty
	 */
	public static IItemType getType( ItemStack stack )
	{
		final Item item = stack.getItem();
		final boolean isHost = item instanceof IItemTypeHost;
		return !isHost && stack.isEmpty() ? IItemType.VANILLA : ( ( IItemTypeHost ) item ).meta();
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
			final T ret = visitor.apply( getType( stack ), stack );
			if( ret != null ) return ret;
		}
		return null;
	}
}
