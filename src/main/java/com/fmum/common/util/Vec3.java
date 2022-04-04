package com.fmum.common.util;

/**
 * A simple vector with 3 {@code double} values
 * 
 * @author Giant_Salted_Fish
 */
public class Vec3
{
	public static final ObjPool<Vec3> pool = new ObjPool<Vec3>(() -> new Vec3());
	
	public double x, y, z;
	
	public Vec3() { this(0D); }
	
	public Vec3(double a) { this.x = this.y = this.z = a; }
	
	public Vec3(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vec3(Vec3 v)
	{
		this.x = v.x;
		this.y = v.y;
		this.z = v.z;
	}
	
	public Vec3 set(double a)
	{
		this.x
			= this.y
			= this.z
			= a;
		return this;
	}
	
	public final Vec3 set(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}
	
	public final Vec3 set(Vec3 v)
	{
		this.x = v.x;
		this.y = v.y;
		this.z = v.z;
		return this;
	}
	
	public final Vec3 trans(double x, double y, double z)
	{
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}
	
	public final Vec3 trans(Vec3 v)
	{
		this.x += v.x;
		this.y += v.y;
		this.z += v.z;
		return this;
	}
	
	public final Vec3 sub(Vec3 v)
	{
		this.x -= v.x;
		this.y -= v.y;
		this.z -= v.z;
		return this;
	}
	
	public Vec3 scale(double s)
	{
		this.x *= s;
		this.y *= s;
		this.z *= s;
		return this;
	}
	
	public final Vec3 scale(double x, double y, double z)
	{
		this.x *= x;
		this.y *= y;
		this.z *= z;
		return this;
	}
	
	public final Vec3 scale(Vec3 v)
	{
		this.x *= v.x;
		this.y *= v.y;
		this.z *= v.z;
		return this;
	}
	
	public final Vec3 flip(boolean x, boolean y, boolean z)
	{
		this.x = x ? -this.x : this.x;
		this.y = y ? -this.y : this.y;
		this.z = z ? -this.z : this.z;
		return this;
	}
	
	public Vec3 negate()
	{
		this.x = -this.x;
		this.y = -this.y;
		this.z = -this.z;
		return this;
	}
	
	public Vec3 normalize() { return this.scale(1D / this.length()); }
	
	public final double dot(Vec3 v) { return this.x * v.x + this.y * v.y + this.z * v.z; }
	
	public final Vec3 cross(Vec3 v)
	{
		double x = this.y * v.z - this.z * v.y;
		double y = this.z * v.x - this.x * v.z;
		this.z = this.x * v.y - this.y * v.x;
		this.y = y;
		this.x = x;
		return this;
	}
	
	public double lengthSquared() { return this.x * this.x + this.y * this.y + this.z * this.z; }
	
	public final double length() { return Math.sqrt(this.lengthSquared()); }
	
	public boolean nonZero() { return this.x != 0D || this.y != 0D || this.z != 0D; }
	
	public final boolean equals(Vec3 v) { return this.x == v.x && this.y == v.y && this.z == v.z; }
	
	@Override
	public boolean equals(Object o) { return o instanceof Vec3 && this.equals((Vec3)o); }
	
	@Override
	public String toString() { return "(" + this.x + ", " + this.y + ", " + this.z + ")"; }
}
