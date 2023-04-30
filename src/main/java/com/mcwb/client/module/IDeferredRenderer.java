package com.mcwb.client.module;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@FunctionalInterface
public interface IDeferredRenderer
{
	@SideOnly( Side.CLIENT )
	void render();
	
	@SideOnly( Side.CLIENT )
	default void prepare() { }
	
	/**
	 * Called before first person render to determine the order of deferred render.
	 */
	@SideOnly( Side.CLIENT )
	default float priority() { return 0F; }
}
