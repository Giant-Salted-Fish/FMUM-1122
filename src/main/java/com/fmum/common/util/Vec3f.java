package com.fmum.common.util;

/**
 * A simple vector with 3 float values
 * 
 * @author Giant_Salted_Fish
 */
public final class Vec3f
{
	public static final ObjPool<Vec3f> pool = new ObjPool<Vec3f>(() -> new Vec3f());
	
	public float x, y, z;
	
	public Vec3f() { this(0F); }
	
	public Vec3f(float a) { this.x = this.y = this.z = a; }
	
	public Vec3f(float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vec3f set(float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}
	
	public Vec3f trans(float x, float y, float z)
	{
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}
	
	public Vec3f trans(Vec3f v)
	{
		this.x += v.x;
		this.y += v.y;
		this.z += v.z;
		return this;
	}
	
	public Vec3f sub(Vec3f v)
	{
		this.x -= v.x;
		this.y -= v.y;
		this.z -= v.z;
		return this;
	}
	
	public Vec3f scale(float s)
	{
		this.x *= s;
		this.y *= s;
		this.z *= s;
		return this;
	}
	
	public float lengthSquared() { return this.x * this.x + this.y * this.y + this.z * this.z; }
	
	public float length() { return (float)Math.sqrt(this.lengthSquared()); }
}