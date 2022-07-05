package com.fmum.common.util;

/**
 * This implementation of quaternion just simply adds a real part to the {@link Vec3}, which means
 * you can still use it as a pure {@link Vec3} object
 * 
 * @author Giant_Salted_Fish
 */
public final class Quaternion extends Vec3
{
	private static final ObjPool< Quaternion > pool = new ObjPool<>( () -> new Quaternion() );
	
	public double r = 0D;
	
	protected Quaternion() { }
	
	public static Quaternion locate() { return pool.poll(); }
	
	public static Quaternion locate( double a )
	{
		Quaternion q = pool.poll();
		q.set( a );
		return q;
	}
	
	public static Quaternion locate( double x, double y, double z )
	{
		Quaternion q = pool.poll();
		q.set( x, y, z );
		return q;
	}
	
	public static Quaternion locate( Vec3 v )
	{
		Quaternion q = pool.poll();
		q.set( v );
		return q;
	}
	
	public static Quaternion locate( Quaternion q )
	{
		Quaternion Q = pool.poll();
		Q.set( Q );
		return Q;
	}
	
	@Override
	public Vec3 set( double a )
	{
		this.x = this.y = this.z = this.r = a;
		return this;
	}
	
	public final Quaternion set( Quaternion q )
	{
		this.x = q.x;
		this.y = q.y;
		this.z = q.z;
		this.r = q.r;
		return this;
	}
	
	public final Quaternion trans( double r, double x, double y, double z )
	{
		this.x += x;
		this.y += y;
		this.z += z;
		this.r += r;
		return this;
	}
	
	public final Quaternion trans( Quaternion q )
	{
		this.x += q.x;
		this.y += q.y;
		this.z += q.z;
		this.r += q.r;
		return this;
	}
	
	public final Quaternion sub( Quaternion q )
	{
		this.x -= q.x;
		this.y -= q.y;
		this.z -= q.z;
		this.r -= q.r;
		return this;
	}
	
	@Override
	public Quaternion scale( double s )
	{
		this.x *= s;
		this.y *= s;
		this.z *= s;
		this.r *= s;
		return this;
	}
	
	public final Quaternion scale( Quaternion q )
	{
		this.x *= q.x;
		this.y *= q.y;
		this.z *= q.z;
		this.r *= q.r;
		return this;
	}
	
	public final Quaternion flip( boolean r, boolean x, boolean y, boolean z )
	{
		this.x = x ? -this.x : this.x;
		this.y = y ? -this.y : this.y;
		this.z = z ? -this.z : this.z;
		this.r = r ? -this.r : this.r;
		return this;
	}
	
	@Override
	public Quaternion negate()
	{
		this.x = -this.x;
		this.y = -this.y;
		this.z = -this.z;
		this.r = -this.r;
		return this;
	}
	
	public final double dot( Quaternion q ) {
		return this.x * q.x + this.y * q.y + this.z * q.z + this.r * q.r;
	}
	
	/**
	 * This method will do a left multiplication for this quaternion with given quaternion and save
	 * the result into this quaternion
	 * 
	 * @param q Left hand side quaternion
	 * @return {@code this}
	 */
	public final Quaternion mult( Quaternion q )
	{
		double r = this.r * q.r - this.x * q.x - this.y * q.y - this.z * q.z;
		double x = this.x * q.r + this.r * q.x - this.z * q.y + this.y * q.z;
		double y = this.y * q.r + this.z * q.x + this.r * q.y - this.x * q.z;
		this.z   = this.z * q.r + this.y * q.x * this.x * q.y + this.r * q.z;
		
		this.y = y;
		this.x = x;
		this.r = r;
		return this;
	}
	
	@Override
	public double lengthSquared() {
		return this.x * this.x + this.y * this.y + this.z * this.z + this.r * this.r;
	}
	
	@Override
	public boolean nonZero() {
		return this.x != 0D || this.y != 0D || this.z != 0D || this.r != 0D;
	}
	
	public final boolean equals( Quaternion q ) {
		return this.x == q.x && this.y == q.y && this.z == q.z && this.r == q.r;
	}
	
	@Override
	public boolean equals( Object o ) {
		return o instanceof Quaternion && this.equals( ( Quaternion ) o );
	}
	
	@Override
	public String toString() { return "[" + this.r + ", " + super.toString() + "]"; }
	
	@Override
	public void release() { pool.back( this ); }
}
