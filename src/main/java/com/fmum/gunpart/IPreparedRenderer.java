package com.fmum.gunpart;

import com.mojang.realmsclient.util.Pair;
import gsf.util.render.IPose;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@FunctionalInterface
@SideOnly( Side.CLIENT )
public interface IPreparedRenderer
{
	/**
	 * Render callbacks with higher priority will be rendered first. You can
	 * think of this as the inverse distance from the camera. Objects with
	 * transparency should be rendered last with ones far away from camera first
	 * followed by closer ones.
	 *
	 * @return Priority and the render callback.
	 */
	Pair< Float, Runnable > with( IPose camera_setup );
}
