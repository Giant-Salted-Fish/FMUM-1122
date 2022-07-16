package com.fmum.common.item;

import com.fmum.common.MetaHost;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@FunctionalInterface
public interface MetaHostItem extends MetaHost
{
	@Override
	public MetaItem meta();
	
	public static MetaItem getMeta( ItemStack stack )
	{
		final Item item = stack.getItem();
		return item instanceof MetaHostItem ? ( ( MetaHostItem ) item ).meta() : MetaItem.NONE;
	}
}
