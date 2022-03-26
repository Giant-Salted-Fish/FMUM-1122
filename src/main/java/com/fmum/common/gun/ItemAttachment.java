package com.fmum.common.gun;

import com.fmum.common.type.ItemHoldable;

import net.minecraft.item.ItemStack;

public class ItemAttachment extends ItemHoldable implements ItemGunPart
{
	public final TypeAttachment type;
	
	public ItemAttachment(TypeAttachment type) { this.type = type; }
	
	@Override
	public TypeAttachment getType() { return this.type; }
	
	@Override
	public void renderFP(ItemStack stack) { this.type.model.renderFP(stack, this.type); }
	
	@Override
	public void render() { this.type.model.render(); }
}
