package com.mcwb.util;

import javax.vecmath.AxisAngle4f;

public final class AngleAxis4f extends AxisAngle4f
{
	public static final AngleAxis4f ORIGIN = new AngleAxis4f( 0F, 0F, 1F, 0F );
	
	private static final long serialVersionUID = 6249913083904720177L;
	
	public AngleAxis4f() { }
	
	/**
	 * Initialize this instance with given euler rotation applied in order ZXY
	 */
	public AngleAxis4f( float x, float y, float z )
	{
		final Mat4f mat = Mat4f.locate();
		mat.setIdentity();
		mat.eulerRotateYXZ( x, y, z );
		this.set( mat );
		mat.release();
	}
	
	public AngleAxis4f( float angle, float axisX, float axisY, float axisZ )
	{
		final float s = 1F / ( float ) Math.sqrt( axisX * axisX + axisY * axisY + axisZ * axisZ );
		
		this.angle = angle;
		this.x = axisX * s;
		this.y = axisY * s;
		this.z = axisZ * s;
	}
	
	public AngleAxis4f( float angle, Vec3f axis ) { super( axis, angle ); }
}
