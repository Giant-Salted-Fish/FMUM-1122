package com.mcwb.client.render;

import com.mcwb.util.Mat4f;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @see IRenderer
 * @author Giant_Salted_Fish
 */
@FunctionalInterface
public interface IAnimator
{
	@SideOnly( Side.CLIENT )
	public void applyChannel( String channel, float smoother, Mat4f dst );
}
