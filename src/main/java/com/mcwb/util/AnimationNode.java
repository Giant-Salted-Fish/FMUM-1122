package com.mcwb.util;

public class AnimationNode implements IAnimation
{
	public final Vec3f pos = new Vec3f();
	
	public float time = 0F;
	
	public AnimationNode() { }
	
	public AnimationNode( float x, float y, float z, float time )
	{
		this.pos.set( x, y, z );
		this.time = time;
	}
	
	public AnimationNode( AnimationNode src, float time )
	{
		this.pos.set( src.pos );
		this.time = time;
	}
	
	@Override
	public float getTime() { return this.time; }
}
