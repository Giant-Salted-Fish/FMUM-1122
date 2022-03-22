package com.fmum.common.util;

/**
 * A simple vector with 3 float values
 * 
 * @author Giant_Salted_Fish
 */
public class Vec3f
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
	
	public final Vec3f set(float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}
	
	public final Vec3f set(Vec3f v)
	{
		this.x = v.x;
		this.y = v.y;
		this.z = v.z;
		return this;
	}
	
	public final Vec3f trans(float x, float y, float z)
	{
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}
	
	public final Vec3f trans(Vec3f v)
	{
		this.x += v.x;
		this.y += v.y;
		this.z += v.z;
		return this;
	}
	
	public final Vec3f sub(Vec3f v)
	{
		this.x -= v.x;
		this.y -= v.y;
		this.z -= v.z;
		return this;
	}
	
	public final Vec3f scale(float s)
	{
		this.x *= s;
		this.y *= s;
		this.z *= s;
		return this;
	}
	
	public final float dot(Vec3f v) { return this.x * v.x + this.y * v.y + this.z * v.z; }
	
	public final Vec3f cross(Vec3f v)
	{
		float x = this.y * v.z - this.z * v.y;
		float y = this.z * v.x - this.x * v.z;
		this.z = this.x * v.y - this.y * v.x;
		this.y = y;
		this.x = x;
		return this;
	}
	
	public float lengthSquared() { return this.x * this.x + this.y * this.y + this.z * this.z; }
	
	public final float length() { return (float)Math.sqrt(this.lengthSquared()); }
}