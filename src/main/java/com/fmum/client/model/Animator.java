package com.fmum.client.model;

import com.fmum.common.item.MetaItem;
import com.fmum.common.util.Animation;

import net.minecraft.item.ItemStack;

public interface Animator
{
	// TODO: remove this maybe?
	public default void launchAnimation( Animation animation ) { }
	
	public default void itemTick( ItemStack stack, MetaItem meta ) { }
}
