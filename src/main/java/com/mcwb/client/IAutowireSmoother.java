package com.mcwb.client;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Obtain partial tick time for rendering.
 * 
 * @author Giant_Salted_Fish
 */
public interface IAutowireSmoother
{
	/**
	 * @return Render partial tick time.
	 */
	@SideOnly( Side.CLIENT )
	public default float smoother() { return MCWBClient.MC.getRenderPartialTicks(); }
}
