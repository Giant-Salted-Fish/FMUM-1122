package com.mcwb.client.module;

import java.util.Collection;

import com.mcwb.client.render.IAnimator;
import com.mcwb.client.render.IRenderer;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IModuleRenderer< T > extends IRenderer
{
	public static final String CHANNEL_INSTALL = "install";
	public static final String CHANNEL_MODIFY = "modify";
	
	@SideOnly( Side.CLIENT )
	public void prepareRender(
		T contexted,
		IAnimator animator,
		Collection< IDeferredRenderer > renderQueue0,
		Collection< IDeferredPriorityRenderer > renderQueue1
	);
}
