package com.fmum.common.module;

import com.fmum.common.util.Vec3;

public final class Hitbox
{
	public final Vec3[] vertices;
	
	public double radiusSquared = 0D;
	
	public Hitbox(Vec3... vertices)
	{
		this.vertices = vertices;
		for(Vec3 v : vertices)
			this.radiusSquared = Math.max(this.radiusSquared, v.lengthSquared());
	}
	
	public static Hitbox parse(String[] split, int cursor)
	{
		final Vec3[] verts = new Vec3[8];
		for(int i = 0; i < 8; ++i)
			verts[i] = Vec3.parse(split[cursor + i + 1]);
		return new Hitbox(verts);
	}
}
