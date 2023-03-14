package com.mcwb.client.module;

import java.util.Collection;

import com.mcwb.client.render.IAnimator;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IModuleRenderer< T >
{
	public static final String CHANNEL_INSTALL = "__install__";
	public static final String CHANNEL_MODIFY = "__modify__";
	
	@SideOnly( Side.CLIENT )
	public void prepareRender(
		T contexted,
		IAnimator animator,
		Collection< IDeferredRenderer > renderQueue0,
		Collection< IDeferredPriorityRenderer > renderQueue1
	);
}
