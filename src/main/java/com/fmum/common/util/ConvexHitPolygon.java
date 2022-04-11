package com.fmum.common.util;

public final class ConvexHitPolygon
{
	public static final double ERROR = Double.longBitsToDouble(
		(Double.doubleToLongBits(Math.PI * 2) >>> 52) - 52 << 52
	) * 16;
	
	public final Vec3[] vertices;
	
	protected final Vec3
		v0 = Vec3.get(),
		v1 = Vec3.get(),
		v2 = Vec3.get();
	
	public ConvexHitPolygon(Vec3... vertices) { this.vertices = vertices; }
	
	/**
	 * @param polygon Another polygon
	 * @return {@code true} if these two polygons collide in space(have intersection)
	 */
	public boolean collideWith(ConvexHitPolygon polygon) {
		return this.linesCollideWith(polygon) || polygon.linesCollideWith(this);
	}
	
	/**
	 * @param polygon Polygon that restricts the test area
	 * @return {@code true} if any of the lines of this polygon collides with the given polygon
	 */
	public boolean linesCollideWith(ConvexHitPolygon polygon)
	{
		Vec3 p0;
		Vec3 p1 = this.vertices[0];
		final Vec3 direction = this.v0;
		final Vec3 intersection = this.v1;
		for(int i = this.vertices.length; i-- > 0; p1 = p0)
		{
			p0 = this.vertices[i];
			direction.set(p0).sub(p1);
			if(
				polygon.hitInside(p0, direction, intersection)
				&& Util.inBoxSpace(p1, p1, intersection)
			) return true;
		}
		return false;
	}
	
	/**
	 * @param lineOrigin Origin of the line
	 * @param lineDirection Direction of the line
	 * @param dst Destination vector to store intersection point
	 * @return
	 *     {@code true} if the intersection of this polygon and the given line is inside this
	 *     polygon
	 */
	public boolean hitInside(Vec3 lineOrigin, Vec3 lineDirection, Vec3 dst)
	{
		// Get normal vector of this plane
		this.v0.set(this.vertices[0]).sub(this.vertices[1]).cross(
			this.v1.set(this.vertices[2]).sub(this.vertices[1])
		);
		
		Util.intersectionOfLineAndPlane(
			lineOrigin, lineDirection,
			this.vertices[0], this.v0,
			dst
		);
		
		return this.isInside(dst);
	}
	
	/**
	 * @param point A point that is in the same plane of this polygon
	 * @return {@code true} if the point is inside this polygon
	 */
	public boolean isInside(Vec3 point)
	{
		// Avoid 0 length direction vector
		for(Vec3 p : this.vertices)
			if(p.equals(point))
				return true;
		
		// Add up the angles form each edge to the given point to check if it gets close to 2PI
		double angle = 0D;
		Vec3 OA = this.v0;
		Vec3 OB = this.v1.set(this.vertices[0]).sub(point);
		for(int i = this.vertices.length; i-- > 0; OB.set(OA))
			angle += OA.set(this.vertices[i]).sub(point).angle(OB);
		
		return Math.abs(Math.PI * 2 - angle) <= ERROR;
	}
	
	/**
	 * @param point A point in space
	 * @return Solid angle of this polygon from the given point
	 */
	public double solidAngle(Vec3 point)
	{
		this.v0.set(this.vertices[0]).sub(point);
		
		double angle = 0D;
		Vec3 v1 = this.v1;
		Vec3 v2 = this.v2.set(this.vertices.length - 1).sub(point);
		for(int i = this.vertices.length - 1; --i > 0; v2.set(v1))
			angle += v1.set(this.vertices[i]).sub(point).solidAngle(v2, this.v0);
		
		return angle;
	}
	
	public Vec3 origin() { return this.vertices[0]; }
}
