package com.fmum.common.gun;

import net.minecraft.item.Item;

public final class ItemGun extends Item
{
	public final TypeGun type;
	
	public ItemGun(TypeGun type) { this.type = type; }
}
