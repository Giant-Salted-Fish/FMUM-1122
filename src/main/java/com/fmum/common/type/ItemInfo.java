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
	
	/**
	 * Types like {@link com.fmum.common.module.TypeModular} could require to store additional
	 * information in stack's nbt tag. This checks if the tag of the given stack is ready or not.
	 * 
	 * @param stack Item stack to check tag
	 * @return {@code true} if tag is ready or no requirement on tag
	 */
	default public boolean tagReady(ItemStack stack) { return true; }
	
	/**
	 * @return {@code true} if view bobbing should be disabled when holding this item
	 */
	default public boolean shouldDisableViewBobbing() { return false; }
	
	/**
	 * Called when player switched to this item
	 */
	default public void onTakeOut(ItemStack stack) { }
	
	default public void renderFP(ItemStack stack)
	{
		final TypeInfo type = this.getType();
		type.model.renderFP(stack, type);
	}
	
	default public void render() { this.getType().model.render(); }
}
