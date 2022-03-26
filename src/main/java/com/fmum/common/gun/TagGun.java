package com.fmum.common.gun;

import com.fmum.common.module.TagModular;

import net.minecraft.item.ItemStack;

public abstract class TagGun extends TagModular
{
	// TODO
	public static final byte NUM_STATES = TagModular.NUM_STATES + 0;
	
	private TagGun() { }
	
	public static void setupTag(ItemStack stack)
	{
		stack.getTagCompound().setTag(
			TAG,
			((ItemGun)stack.getItem()).type.genTag(stack.getItemDamage())
		);
	}
}
