package com.fmum.client.render;

import com.fmum.common.item.MetaItem;
import com.fmum.common.util.Animation;
import com.fmum.common.util.Vec3;

import net.minecraft.item.ItemStack;

public interface Animator
{
	// TODO: remove this maybe?
	public default void launchAnimation( Animation animation ) { }
	
	public default void itemTick( ItemStack stack, MetaItem meta ) { }
	
	public default void getChannelVec( String channel, Vec3 dst ) { dst.set( 0D ); }
}
