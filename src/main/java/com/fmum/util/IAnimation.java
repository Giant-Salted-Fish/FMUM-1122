package com.fmum.util;

public interface IAnimation
{
	void update( float progress );
	
	void getPos( String channel, Vec3f dst );
	
	void getRot( String channel, Quat4f dst );
	
	float getFactor( String channel );
}
