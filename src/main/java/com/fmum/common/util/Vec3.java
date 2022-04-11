package com.fmum.common.util;

/**
 * A simple vector with 3 {@code double} values
 * 
 * @author Giant_Salted_Fish
 */
public class Vec3 implements Releasable
{
	private static final ObjPool<Vec3> pool = new ObjPool<Vec3>(() -> new Vec3());
	
	public double x, y, z;
	
	protected Vec3() { }
	
	public static Vec3 get() { return pool.poll(); }
	
	public static Vec3 get(double a) { return pool.poll().set(a); }
	
	public static Vec3 get(double x, double y, double z) { return pool.poll().set(x, y, z); }
	
	public static Vec3 get(Vec3 v) { return pool.poll().set(v); }
	
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
	
	/**
	 * @param v Another vector that has the same origin
	 * @return Angle between this vector and the given vector in radians
	 */
	public final double angle(Vec3 v) {
		return Math.acos(this.dot(v) / this.length() / v.length());
	}
	
	public final double solidAngle(Vec3 v1, Vec3 v2)
	{
		double a = this.angle(v1);
		double b = this.angle(v2);
		double c = v1.angle(v2);
		double s = (a + b + c) / 2D;
		
		return 4D * Math.atan(
			Math.sqrt(
				Math.tan(s / 2)
					* Math.tan((s - a) / 2)
					* Math.tan((s - b) / 2)
					* Math.tan((s - c) / 2)
			)
		);
	}
	
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
	
	@Override
	public void release() { pool.back(this); }
	
	public static Vec3 parse(String text)
	{
		String[] split = text.split(",");
		return get(
			Double.parseDouble(split[0].substring(1)),
			Double.parseDouble(split[1]),
			Double.parseDouble(split[2].substring(0, split[2].length() - 1))
		);
	}
}
