package gsf.util.math;

import net.minecraft.util.math.MathHelper;

import javax.vecmath.Vector3f;

public final class Vec3f extends Vector3f
{
	public static final Vec3f ORIGIN = new Vec3f();
	
	
	public static Vec3f allocate() {
		return new Vec3f();
	}
	
	public static void release( Vec3f vec ) {
		// TODO: obj pool?
	}
	
	
	public Vec3f() { }
	
	public Vec3f( float x, float y, float z ) {
		super( x, y, z );
	}
	
	public void add( float x, float y, float z )
	{
		this.x += x;
		this.y += y;
		this.z += z;
	}
	
	public void setZero()
	{
		this.x = 0.0F;
		this.y = 0.0F;
		this.z = 0.0F;
	}
	
	/**
	 * @return Whether the length of the vector is {@code 0.0F}.
	 */
	public boolean isOrigin() {
		return this.x == 0.0F && this.y == 0.0F && this.z == 0.0F;
	}
	
	/**
	 * Get pitch (x) and yaw (y) angle of this vector in degrees. Notice that
	 * this will not clear roll (z) angle of the destination vector.
	 */
	public void getEulerAngle( Vec3f dst )
	{
		final float pitch = ( float ) -Math.asin( this.y * MoreMath.fastInvSqrt( this.lengthSquared() ) );
		final float yaw = ( float ) MathHelper.atan2( this.x, this.z );
		dst.x = MoreMath.toDegrees( pitch );
		dst.y = MoreMath.toDegrees( yaw );
	}
}
