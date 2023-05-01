package com.fmum.client.module;

import java.util.Collection;

import com.fmum.client.render.IAnimator;
import com.fmum.util.Mat4f;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IModuleRenderer< T >
{
	@SideOnly( Side.CLIENT )
	void getTransform( Mat4f dst );
	
	@SideOnly( Side.CLIENT )
	void prepareRender(
		T contexted, IAnimator animator,
		Collection< IDeferredRenderer > renderQueue0,
		Collection< IDeferredRenderer > renderQueue1
	);
}
