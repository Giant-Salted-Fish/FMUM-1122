package com.fmum.common.util;

/**
 * A simple vector with 3 {@code double} values
 * 
 * @author Giant_Salted_Fish
 */
public class Vec3f implements Releasable
{
	private static final ObjPool< Vec3f > pool = new ObjPool<>( () -> new Vec3f() );
	
	public float
		x = 0F,
		y = 0F,
		z = 0F;
	
	protected Vec3f() { }
	
	public static Vec3f get() { return pool.poll(); }
	
	public static Vec3f get( float x, float y, float z ) { return pool.poll().set( x, y, z ); }
	
	public final Vec3f set( float a )
	{
		this.x
			= this.y
			= this.z
			= a;
		return this;
	}
	
	public final Vec3f set( float x, float y, float z )
	{
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}
	
	public final Vec3f set( Vec3f v )
	{
		this.x = v.x;
		this.y = v.y;
		this.z = v.z;
		return this;
	}
	
	public final Vec3f trans( Vec3f v )
	{
		this.x += v.x;
		this.y += v.y;
		this.z += v.z;
		return this;
	}
	
	public final Vec3f sub( Vec3f v )
	{
		this.x -= v.x;
		this.y -= v.y;
		this.z -= v.z;
		return this;
	}
	
	public final Vec3f scale( float s )
	{
		this.x *= s;
		this.y *= s;
		this.z *= s;
		return this;
	}
	
	public final Vec3f flip( boolean x, boolean y, boolean z )
	{
		this.x = x ? -this.x : this.x;
		this.y = y ? -this.y : this.y;
		this.z = z ? -this.z : this.z;
		return this;
	}
	
	public final Vec3f cross( Vec3f v )
	{
		float x = this.y * v.z - this.z * v.y;
		float y = this.z * v.x - this.x * v.z;
		this.z = this.x * v.y - this.y * v.x;
		this.y = y;
		this.x = x;
		return this;
	}
	
	public final Vec3f normalize()
	{
		return this.scale(
			1F / ( float ) Math.sqrt( this.x * this.x + this.y * this.y + this.z * this.z )
		);
	}
	
	public boolean nonZero() { return this.x != 0F || this.y != 0F || this.z != 0F; }
	
	public final boolean equals( Vec3f v ) {
		return this.x == v.x && this.y == v.y && this.z == v.z;
	}
	
	@Override
	public boolean equals( Object o ) { return o instanceof Vec3f && this.equals( ( Vec3f ) o ); }
	
	@Override
	public void release() { pool.back( this ); }
}