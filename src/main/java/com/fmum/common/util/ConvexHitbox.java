package com.fmum.common.util;

public final class ConvexHitbox
{
	public static final double ERROR = Double.longBitsToDouble(
		(Double.doubleToLongBits(Math.PI * 4) >>> 52) - 52 << 52
	) * 16D;
	
	/**
	 * 6 rectangular surfaces of the box
	 */
	public final ConvexHitPolygon[] rects;
	
	/**
	 * @param vertices 8 vertices of the hit box in ToolBox shape box vertex order
	 */
	public ConvexHitbox(Vec3... vertices)
	{
		this.rects = new ConvexHitPolygon[] {
			new ConvexHitPolygon(vertices[0], vertices[1], vertices[2], vertices[3]),
			new ConvexHitPolygon(vertices[4], vertices[5], vertices[6], vertices[7]),
			new ConvexHitPolygon(vertices[0], vertices[3], vertices[7], vertices[4]),
			new ConvexHitPolygon(vertices[1], vertices[2], vertices[6], vertices[5]),
			new ConvexHitPolygon(vertices[0], vertices[1], vertices[5], vertices[4]),
			new ConvexHitPolygon(vertices[2], vertices[3], vertices[7], vertices[6])
		};
	}
	
	public ConvexHitbox(ConvexHitPolygon... rects) { this.rects = rects; }
	
	public boolean conflictWith(ConvexHitbox box) {
		return this.collideWith(box) || this.isInside(box.origin()) || box.isInside(this.origin());
	}
	
	public boolean collideWith(ConvexHitbox box)
	{
		// Check if any of the rectangular collide with those in given box
		for(ConvexHitPolygon p0 : this.rects)
			for(ConvexHitPolygon p1 : box.rects)
				if(p0.collideWith(p1))
					return true;
		return false;
	}
	
	public boolean isInside(Vec3 point)
	{
		double angle = 0D;
		for(ConvexHitPolygon p : this.rects)
			angle += p.solidAngle(point);
		return Math.abs(Math.PI * 4D - angle) < ERROR;
	}
	
	public Vec3 origin() { return this.rects[0].origin(); }
}
