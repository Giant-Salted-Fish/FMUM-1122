package com.mcwb.client.module;

import com.mcwb.util.IReleasable;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@FunctionalInterface
public interface IDeferredRenderer extends IReleasable
{
	@SideOnly( Side.CLIENT )
	public void render();
	
	@Override
	public default void release() { }
}
