package com.fmum.common.item;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;

public abstract class ItemArmorBase extends ItemArmor implements HostItem
{
	// TODO: proxy item armor
	public ItemArmorBase() { super( ArmorMaterial.IRON, 1, EntityEquipmentSlot.CHEST ); }
}
