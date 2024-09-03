package com.fmum.gunpart;

import gsf.util.animation.IPoseSetup;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@FunctionalInterface
@SideOnly( Side.CLIENT )
public interface IRenderCallback
{
	void render();
	
	/**
	 * Render callbacks with higher priority will be rendered first. You can
	 * think of this as the inverse distance from the camera. Objects with
	 * transparency should be rendered last with ones far away from camera first
	 * followed by closer ones.
	 */
	default float getPriority( IPoseSetup camera_setup ) {
		return 0.0F;
	}
}
