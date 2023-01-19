package com.mcwb.client;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Obtain partial tick time for rendering
 * 
 * @author Giant_Salted_Fish
 */
@SideOnly( Side.CLIENT )
public interface IAutowireSmoother {
	public default float smoother() { return MCWBClient.MC.getRenderPartialTicks(); }
}
