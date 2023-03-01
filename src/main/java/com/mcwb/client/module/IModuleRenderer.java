package com.mcwb.client.module;

import java.util.Collection;

import com.mcwb.client.render.IAnimator;
import com.mcwb.client.render.IRenderer;
import com.mcwb.common.meta.IContexted;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IModuleRenderer< T extends IContexted > extends IRenderer
{
	public static final String CHANNEL_INSTALL = "install_trans";
	
	@SideOnly( Side.CLIENT )
	public default void prepareInHandRender(
		T contexted,
		IAnimator animator,
		Collection< IDeferredRenderer > renderQueue0,
		Collection< IDeferredPriorityRenderer > renderQueue1
	) { this.prepareRender( contexted, animator, renderQueue0, renderQueue1 ); }
	
	@SideOnly( Side.CLIENT )
	public void prepareRender(
		T contexted,
		IAnimator animator,
		Collection< IDeferredRenderer > renderQueue0,
		Collection< IDeferredPriorityRenderer > renderQueue1
	);
}
