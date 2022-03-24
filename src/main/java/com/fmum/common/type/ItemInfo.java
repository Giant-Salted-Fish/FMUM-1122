package com.fmum.common.type;

import net.minecraft.item.ItemStack;

/**
 * Base item type for all items accepted by FMUM
 * 
 * @author Giant_Salted_Fish
 */
public interface ItemInfo
{
	/**
	 * @return Type that this item proxy for
	 */
	public TypeInfo getType();
	
	default public void renderFP(ItemStack stack)
	{
		final TypeInfo type = this.getType();
		type.model.renderFP(stack, type);
	}
	
	default public void render() { this.getType().model.render(); }
}
