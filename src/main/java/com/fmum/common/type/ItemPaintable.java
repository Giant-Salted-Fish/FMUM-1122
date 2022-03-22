package com.fmum.common.type;

import net.minecraft.item.ItemStack;

public interface ItemPaintable extends ItemInfo
{
	@Override
	public TypePaintable getType();
	
	default public String getRecommendedTranslationKey(ItemStack stack) {
		return this.getType().paintjobs.get(stack.getItemDamage()).translationKey;
	}
}