package com.mcwb.client.render;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * <p> This is the interface between the types and the models which helps to decouple the logic from
 * the rendering. <p>
 * 
 * <p> This actually should be {@link Side#CLIENT} only. But it is not in the end considering the
 * fact that content creators may create their types that inherit this interface and that could
 * crash the load on {@link Side#SERVER}. So for all the sub-types of this interface. </p>
 *  
 * @author Giant_Salted_Fish
 */
@FunctionalInterface
//@SideOnly( Side.CLIENT )
public interface IModel
{
	/**
	 * Simply render all vertices
	 */
	@SideOnly( Side.CLIENT )
	public void render();
}
