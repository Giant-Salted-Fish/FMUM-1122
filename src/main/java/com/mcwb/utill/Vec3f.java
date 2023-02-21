package com.mcwb.utill;

import javax.vecmath.Vector3f;

import com.mcwb.util.Util;

public final class Vec3f extends Vector3f
{
	public static final Vec3f ORIGIN = new Vec3f();
	
	private static final long serialVersionUID = 8903442919042309432L;
	
	public Vec3f() { }
	
	public Vec3f( float x, float y, float z ) { super( x, y, z ); }
	
//	public void flip( boolean x, boolean y, boolean z )
//	{
//		this.x = x ? -this.x : this.x;
//		this.y = y ? -this.y : this.y;
//		this.z = z ? -this.z : this.z;
//	}
	
	public boolean nonZero() { return this.x != 0F || this.y != 0F || this.z != 0F; }
	
	public void getEulerAngle( Vector3f dst )
	{
		final float pitch = ( float ) -Math.asin( this.y / this.length() );
		final float yaw = ( float ) Math.atan2( this.x, this.z );
		
		dst.y = yaw * Util.TO_DEGREES;
		dst.x = pitch * Util.TO_DEGREES;
	}
	
	public static Vec3f parse( String text )
	{
		final String[] split = text.split( "," );
		return new Vec3f(
			Float.parseFloat( split[ 0 ].substring( 1 ) ),
			Float.parseFloat( split[ 1 ] ),
			Float.parseFloat( split[ 2 ].substring( 0, split[ 2 ].length() - 1 ) )
		);
	}
}
