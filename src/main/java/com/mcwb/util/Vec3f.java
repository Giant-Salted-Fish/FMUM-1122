package com.mcwb.util;

import org.lwjgl.util.vector.Vector3f;

/**
 * @author Giant_Salted_Fish
 */
public class Vec3f extends Vector3f implements Releasable
{
	public static final Vec3f ORIGIN = new Vec3f();
	
	private static final long serialVersionUID = -70687262444366687L;
	
	private static final ObjPool< Vec3f > POOL = new ObjPool<>( Vec3f::new );
	
	public static Vec3f locate() { return POOL.poll(); }
	
	public static Vec3f locate( float a ) { return POOL.poll().set( a ); }
	
	public static Vec3f locate( float x, float y, float z )
	{
		final Vec3f v = POOL.poll();
		v.set( x, y, z );
		return v;
	}
	
	public Vec3f() { }
	
	public Vec3f( float a ) { this.set( a ); }
	
	public Vec3f( float x, float y, float z ) { super( x, y, z ); }
	
	public Vec3f set( float a )
	{
		this.x
			= this.y
			= this.z
			= a;
		return this;
	}
	
	public Vec3f set( Vector3f src )
	{
		this.x = src.x;
		this.y = src.y;
		this.z = src.z;
		return this;
	}
	
	public Vec3f translate( Vector3f vec )
	{
		this.x += vec.x;
		this.y += vec.y;
		this.z += vec.z;
		return this;
	}
	
	public Vec3f subtract( Vector3f vec )
	{
		this.x -= vec.x;
		this.y -= vec.y;
		this.z -= vec.z;
		return this;
	}
	
	public Vec3f flip( boolean x, boolean y, boolean z )
	{
		this.x = x ? -this.x : this.x;
		this.y = y ? -this.y : this.y;
		this.z = z ? -this.z : this.z;
		return this;
	}
	
	public boolean nonZero() { return this.x != 0F || this.y != 0F || this.z != 0F; }
	
	public float dot( Vector3f vec ) { return dot( this, vec ); }
	
	public final Vec3f cross( Vector3f right, Vector3f dst )
	{
		cross( this, right, dst );
		return this;
	}
	
	/**
	 * @param vec Another vector that has the same origin
	 * @return Angle between this vector and the given vector in radians
	 */
	public final float angle( Vector3f vec ) {
		return ( float ) Math.acos( this.dot( vec ) / this.length() / vec.length() );
	}
	
	public final float solidAngle( Vec3f vec1, Vec3f vec2 )
	{
		float a = this.angle( vec1 );
		float b = this.angle( vec2 );
		float c = vec1.angle( vec2 );
		float s = ( a + b + c ) / 2F;
		
		return 4F * ( float ) Math.atan(
			Math.sqrt(
				Math.tan( s / 2F )
					* Math.tan( ( s - a ) / 2F )
					* Math.tan( ( s - b ) / 2F )
					* Math.tan( ( s - c ) / 2F )
			)
		);
	}
	
	/**
	 * @param dst Save pitch and yaw angle into dst.x and dst.y in degrees
	 * @return {@code this}
	 */
	public final Vec3f getEulerAngle( Vector3f dst )
	{
		float pitch = ( float ) -Math.asin( this.y );
		float yaw = ( float ) Math.atan2( this.x, this.z );
		
		dst.y = yaw * Util.TO_DEGREES;
		dst.x = pitch * Util.TO_DEGREES;
		
		return this;
	}
	
	public final Vec3f toDegrees()
	{
		this.x *= Util.TO_DEGREES;
		this.y *= Util.TO_DEGREES;
		this.z *= Util.TO_DEGREES;
		return this;
	}
	
	public final Vec3f toRadians()
	{
		this.x *= Util.TO_RADIANS;
		this.y *= Util.TO_RADIANS;
		this.z *= Util.TO_RADIANS;
		return this;
	}
	
	@Override
	public void release() { POOL.back( this ); }
	
	public static Vec3f parse( String text )
	{
		String[] split = text.split( "," );
		return new Vec3f(
			Float.parseFloat( split[ 0 ].substring( 1 ) ),
			Float.parseFloat( split[ 1 ] ),
			Float.parseFloat( split[ 2 ].substring( 0, split[ 2 ].length() - 1 ) )
		);
	}
}
