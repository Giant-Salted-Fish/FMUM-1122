package com.fmum.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * @see IItemType
 */
public abstract class FMUMItemBase extends Item {
	public abstract IItem getItemFrom( ItemStack stack );
}
