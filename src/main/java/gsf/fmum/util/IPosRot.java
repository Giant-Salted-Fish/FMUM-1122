package com.fmum.util;

public interface IPosRot
{
	void getPos( Vec3f dst );

	void getRot( Quat4f dst );

	void getAsMat( Mat4f dst );

	void translate( Vec3f pos );

	void rotate( Quat4f rot );

	void transform( Mat4f transform );
}
