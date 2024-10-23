package gsf.util.math;

public final class AxisAngle4f extends javax.vecmath.AxisAngle4f
{
	public static final AxisAngle4f IDENTITY = new AxisAngle4f();
	
	
	public static AxisAngle4f allocate() {
		return new AxisAngle4f();
	}
	
	public static void release( AxisAngle4f aa ) {
	
	}
	
	
	public AxisAngle4f() { }
	
	/**
	 * Initialize this instance with given axis and angle.
	 *
	 * @param x The x component of the axis.
	 * @param y The y component of the axis.
	 * @param z The z component of the axis.
	 * @param angle The angle in degrees.
	 */
	public AxisAngle4f( float x, float y, float z, float angle )
	{
		final float axis_len_p2 = ( x * x ) + ( y * y ) + ( z * z );
		final float factor = MoreMath.fastInvSqrt( axis_len_p2 );
		this.x = x * factor;
		this.y = y * factor;
		this.z = z * factor;
		this.angle = MoreMath.toRadians( angle );
	}
	
	/**
	 * Initialize this instance with given euler rotation applied in order ZXY.
	 */
	public static AxisAngle4f ofEulerRot( float pitch, float yaw, float roll )
	{
		final AxisAngle4f aa = new AxisAngle4f();
		aa.set( Quat4f.ofEulerRotYXZ( pitch, yaw, roll ) );
		return aa;
	}
	
	/**
	 * Initialize this instance with given axis and angle.
	 *
	 * @param axis The axis of rotation.
	 * @param angle The angle in degrees.
	 */
	public static AxisAngle4f ofAxisAngle( Vec3f axis, float angle ) {
		return new AxisAngle4f( axis.x, axis.y, axis.z, angle );
	}
}
