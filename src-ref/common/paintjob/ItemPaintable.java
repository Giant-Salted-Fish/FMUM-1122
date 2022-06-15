package com.fmum.common.paintjob;

import com.fmum.common.type.ItemInfo;

import net.minecraft.item.ItemStack;

public interface ItemPaintable extends ItemInfo
{
	@Override
	public TypePaintable getType();
	
	default public String getTranslationKey(ItemStack stack) {
		return this.getType().paintjobs.get(stack.getItemDamage()).translationKey;
	}
}