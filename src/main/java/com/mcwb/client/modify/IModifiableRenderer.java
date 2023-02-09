package com.mcwb.client.modify;

import java.util.Collection;

import com.mcwb.client.render.IAnimator;
import com.mcwb.client.render.IRenderer;
import com.mcwb.common.meta.IContexted;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@FunctionalInterface
//@SideOnly( Side.CLIENT )
public interface IModifiableRenderer< T extends IContexted > extends IRenderer
{
	/**
	 * Primary transform has been applied and texture has been bind
	 */
	@SideOnly( Side.CLIENT )
	public default void renderModule(
		T contexted,
		IAnimator animator,
		Collection< IMultPassRenderer > renderQueue
	) { this.render(); }
}
