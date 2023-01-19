package com.mcwb.util;

public final class ConvexHitPolygon
{
	public static final float ERROR = Float.intBitsToFloat(
		( Float.floatToIntBits( Util.PI * 2F ) >>> 23 ) - 23 << 23
	) * 16F;
	
	public final Vec3f[] vertices;
	
	protected final Vec3f
		v0 = new Vec3f(),
		v1 = new Vec3f(),
		v2 = new Vec3f();
	
	public ConvexHitPolygon( Vec3f... vertices ) { this.vertices = vertices; }
	
	/**
	 * @param polygon Another polygon
	 * @return {@code true} if these two polygons collide in space(have intersection)
	 */
	public boolean collideWith( ConvexHitPolygon polygon ) {
		return this.linesCollideWith( polygon ) || polygon.linesCollideWith( this );
	}
	
	/**
	 * @param polygon Polygon that restricts the test area
	 * @return {@code true} if any of the lines of this polygon collides with the given polygon
	 */
	public boolean linesCollideWith( ConvexHitPolygon polygon )
	{
		Vec3f p0;
		Vec3f p1 = this.vertices[ 0 ];
		final Vec3f direction = this.v0;
		final Vec3f intersection = this.v1;
		for( int i = this.vertices.length; i-- > 0; p1 = p0 )
		{
			p0 = this.vertices[ i ];
			direction.set( p0 );
			direction.subtract( p1 );
			if(
				polygon.hitInside( p0, direction, intersection )
				&& Util.inBoxSpace( p1, p1, intersection )
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
	public boolean hitInside( Vec3f lineOrigin, Vec3f lineDirection, Vec3f dst )
	{
		// Get normal vector of this plane
		this.v0.set( this.vertices[ 0 ] ).subtract( this.vertices[ 1 ] );
		this.v1.set( this.vertices[ 2 ] ).subtract(this.vertices[ 1 ] );
		
		this.v0.cross( this.v1, this.v0 );
		
		Util.intersectionOfLineAndPlane(
			lineOrigin, lineDirection,
			this.vertices[ 0 ], this.v0,
			dst
		);
		
		return this.isInside( dst );
	}
	
	/**
	 * @param point A point that is in the same plane of this polygon
	 * @return {@code true} if the point is inside this polygon
	 */
	public boolean isInside( Vec3f point )
	{
		// Avoid 0 length direction vector
		for( Vec3f p : this.vertices )
			if(p.equals( point ) )
				return true;
		
		// Add up the angles form each edge to the given point to check if it gets close to 2PI
		float angle = 0F;
		final Vec3f OA = this.v0;
		final Vec3f OB = this.v1.set( this.vertices[ 0 ] ).subtract( point );
		
		for( int i = this.vertices.length; i-- > 0; OB.set( OA ) )
			angle += OA.set( this.vertices[ i ] ).subtract( point ).angle( OB );
		
		return Math.abs( Math.PI * 2 - angle ) <= ERROR;
	}
	
	/**
	 * @param point A point in space
	 * @return Solid angle of this polygon from the given point
	 */
	public float solidAngle( Vec3f point )
	{
		this.v0.set( this.vertices[ 0 ] ).subtract( point );
		
		float angle = 0F;
		this.v2.set( this.vertices.length - 1 ).subtract( point );
		
		for( int i = this.vertices.length - 1; --i > 0; this.v2.set( this.v1 ) )
			angle += this.v1.set( this.vertices[ i ] ).subtract( point )
				.solidAngle( this.v2, this.v0 );
		
		return angle;
	}
	
	public Vec3f origin() { return this.vertices[ 0 ]; }
}
