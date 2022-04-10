package com.fmum.common.util;

// TODO: maybe implement Releasable?
public final class ConvexHitbox
{
	public static final double ERROR = Double.longBitsToDouble(
		(Double.doubleToLongBits(Math.PI * 4) >>> 52) - 52 << 52
	) * 16D;
	
	/**
	 * 8 vertices of the box. Sequence by ToolBox shape box: 1 3 6 8 2 7 5 4
	 */
	public final Vec3[] vertices;
	
	protected final Vec3
		v0 = Vec3.get(),
		v1 = Vec3.get(),
		v2 = Vec3.get();
	
	public ConvexHitbox(Vec3... vertices) { this.vertices = vertices; }
	
	public ConvexHitbox(
		int lenX, int lenY, int lenZ,
		float x0, float y0, float z0,
		float x1, float y1, float z1,
		float x2, float y2, float z2,
		float x3, float y3, float z3,
		float x4, float y4, float z4,
		float x5, float y5, float z5,
		float x6, float y6, float z6,
		float x7, float y7, float z7
	) {
		this(
			Vec3.get(0F - x0, 0F + y0, 0F + z0),
			Vec3.get(lenX + x2, 0F + y2, -lenZ - z2),
			Vec3.get(lenX + x5, -lenY - y5, 0F + z5),
			Vec3.get(0F - x7, -lenY - y7, -lenZ - z7),
			Vec3.get(lenX + x1, 0F + y1, 0F + z1),
			Vec3.get(lenX + x6, -lenY - y6, -lenZ - z6),
			Vec3.get(0F - x4, -lenY - y4, 0F + z4),
			Vec3.get(0F - x3, 0F + y3, -lenZ - z3)
		);
	}
	
	public boolean inside(Vec3 point)
	{
		final Vec3[] verts = this.vertices;
		for(int i = 4; i > 0; --i)
			if(
				this.checkInside(
					verts[(i + 1) & 3],
					verts[i & 3],
					verts[i - 1],
					verts[i + 3],
					point
				)
			) return true;
		
		return this.checkInside(verts[0], verts[1], verts[2], verts[3], point);
	}
	
	/**
	 * Require point is not overlap with p0, p1, p2 or p3
	 */
	public boolean checkInside(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, Vec3 point)
	{
		return Math.abs(
			Math.PI * 4D
				- this.v0.set(p0).sub(point).solidAngle(
					this.v1.set(p1).sub(point),
					this.v2.set(p2).sub(point)
				)
				- this.v1.solidAngle(this.v2, this.v0.set(p3).sub(point))
				- this.v2.solidAngle(this.v0, this.v1.set(p0).sub(point))
				- this.v0.solidAngle(this.v1, this.v2.set(p1).sub(point))
		) < ERROR;
	}
}
