package com.fmum.common.gun;

import net.minecraft.item.Item;

public final class ItemGun extends Item
{
	public ItemGun()
	{
		// Guns are not allowed to be stacked
		this.setMaxStackSize(1);
		
		
	}
}
