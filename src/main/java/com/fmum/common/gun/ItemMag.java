package com.fmum.common.gun;

import com.fmum.common.type.ItemHoldable;

import net.minecraft.item.ItemStack;

public class ItemMag extends ItemHoldable implements ItemAmmoContainer
{
	public final TypeMag type;
	
	public ItemMag(TypeMag type) { this.type = type; }
	
	@Override
	public TypeMag getType() { return this.type; }
	
	@Override
	public void renderFP(ItemStack stack) { this.type.model.renderFP(stack, this.type); }
	
	@Override
	public void render() { this.type.model.render(); }
}
