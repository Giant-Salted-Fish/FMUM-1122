package com.fmum.common.util;

public final class ConvexHitPolygon
{
	public static final double ERROR = Double.longBitsToDouble(
		(Double.doubleToLongBits(Math.PI * 2) >>> 52) - 52 << 52
	) * 16;
	
	public final Vec3[] vertices;
	
	protected final Vec3
		v0 = Vec3.get(),
		v1 = Vec3.get();
	
	public ConvexHitPolygon(Vec3... vertices) { this.vertices = vertices; }
	
	public boolean isInside(Vec3 point)
	{
		// Avoid 0 length vector
		final Vec3[] verts = this.vertices;
		for(Vec3 p : verts)
			if(p.equals(point))
				return true;
		
		double angle = 0D;
		for(int i = verts.length; i > 0; --i)
		{
			Vec3 p0 = verts[i % verts.length];
			Vec3 p1 = verts[i - 1];
			
			angle += this.v0.set(p0).sub(point).angle(this.v1.set(p1).sub(point));
		}
		
		return Math.abs(Math.PI * 2 - angle) <= ERROR;
	}
	
	public boolean hitInside(
		double posX, double posY, double posZ,
		double directX, double directY, double directZ
	) {
		final Vec3[] verts = this.vertices;
		this.v0.set(verts[0]).sub(verts[1]);
		this.v1.set(verts[2]).sub(verts[1]);
		this.v1.cross(v0);
		
		this.v0.set(verts[0]);
		Vec3 point = Vec3.get();
		Util.getLinePlaneIntersection(
			posX, posY, posZ,
			directX, directY, directZ,
			this.v0.x, this.v0.y, this.v0.z,
			this.v1.x, this.v1.y, this.v1.z,
			point
		);
		
		boolean ret = this.isInside(point);
		point.release();
		return ret;
	}
}
