package com.mcwb.util;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Vector3f;

public final class AngleAxis4f extends AxisAngle4f
{
	public static final AngleAxis4f ORIGIN = new AngleAxis4f( 0F, 0F, 1F, 0F );
	
	private static final long serialVersionUID = 6249913083904720177L;
	
	public AngleAxis4f() { }
	
	public AngleAxis4f( float angle, float axisX, float axisY, float axisZ )
	{
		final float s = 1F / ( float ) Math.sqrt( axisX * axisX + axisY * axisY + axisZ * axisZ );
		
		this.angle = angle;
		this.x = axisX * s;
		this.y = axisY * s;
		this.z = axisZ * s;
	}
	
	public AngleAxis4f( float angle, Vector3f axis ) { this( angle, axis.x, axis.y, axis.z ); }
}
