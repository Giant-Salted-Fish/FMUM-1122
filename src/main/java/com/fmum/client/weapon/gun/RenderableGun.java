package com.fmum.client.weapon.gun;

import com.fmum.client.item.RenderableItem;
import com.fmum.client.modular.RenderableModular;
import com.fmum.common.item.MetaItem;

import net.minecraft.item.ItemStack;
import net.minecraft.util.MouseHelper;

public interface RenderableGun extends RenderableItem, RenderableModular
{
	@Override
	public default void onRenderTick( ItemStack stack, MetaItem meta, MouseHelper mouse )
	{
		RenderableItem.super.onRenderTick( stack, meta, mouse );
		this.onRenderTick( stack.getTagCompound(), meta, mouse );
		
		
	}
}
