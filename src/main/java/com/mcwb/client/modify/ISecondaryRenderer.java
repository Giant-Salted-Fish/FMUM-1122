package com.mcwb.client.modify;

import com.mcwb.client.render.IRenderer;
import com.mcwb.util.Vec3f;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@FunctionalInterface
public interface ISecondaryRenderer extends IRenderer, Comparable< ISecondaryRenderer >
{
	@SideOnly( Side.CLIENT )
	public default void prepare() { }
	
	@SideOnly( Side.CLIENT )
	public default Vec3f pos() { return Vec3f.ORIGIN; }
	
	@Override
	@SideOnly( Side.CLIENT )
	public default int compareTo( ISecondaryRenderer o ) {
		return this.pos().x > o.pos().x ? 1 : -1;
	}
}
