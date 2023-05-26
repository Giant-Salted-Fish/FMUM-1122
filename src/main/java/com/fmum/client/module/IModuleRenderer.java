package com.fmum.client.module;

import com.fmum.client.render.IAnimator;
import com.fmum.util.Mat4f;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collection;

public interface IModuleRenderer< T >
{
	@SideOnly( Side.CLIENT )
	void getTransform( Mat4f dst );
	
	@SideOnly( Side.CLIENT )
	void prepareRender(
		T module, IAnimator animator,
		Collection< IDeferredRenderer > renderQueue0,
		Collection< IDeferredRenderer > renderQueue1
	);
}
