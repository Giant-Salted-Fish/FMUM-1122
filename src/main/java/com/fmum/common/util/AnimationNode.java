package com.fmum.common.util;

public class AnimationNode implements Animation
{
	public final Vec3 pos = Vec3.get();
	
	public double time = 0D;
	
	public AnimationNode() { }
	
	public AnimationNode( double x, double y, double z, double time )
	{
		this.pos.set(x, y, z);
		this.time = time;
	}
	
	public AnimationNode( AnimationNode src, double time )
	{
		this.pos.set(src.pos);
		this.time = time;
	}
	
	@Override
	public double getTime() { return this.time; }
}
