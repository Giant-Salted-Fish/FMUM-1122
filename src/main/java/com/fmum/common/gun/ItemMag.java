package com.fmum.common.gun;

import com.fmum.common.type.ItemHoldable;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMag extends ItemHoldable implements ItemAmmoContainer
{
	public final TypeMag type;
	
	public ItemMag(TypeMag type) { this.type = type; }
	
	@Override
	public TypeMag getType() { return this.type; }
	
	@Override
	@SideOnly(Side.CLIENT)
	public void renderFP(ItemStack stack) { this.type.model.renderFP(stack, this.type); }
	
	@Override
	@SideOnly(Side.CLIENT)
	public void render() { this.type.model.render(); }
}
