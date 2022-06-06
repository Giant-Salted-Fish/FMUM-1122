package com.fmum.common.item;

import com.fmum.common.Host;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@FunctionalInterface
public interface HostItem extends Host
{
	@Override
	public MetaItem meta();
	
	public static MetaItem getMeta( ItemStack stack )
	{
		final Item item = stack.getItem();
		return item instanceof HostItem ? ( ( HostItem ) item ).meta() : MetaItem.NONE;
	}
}
