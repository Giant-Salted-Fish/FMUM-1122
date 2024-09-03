package gsf.util.math;

import net.minecraft.util.math.MathHelper;

/**
 * {@link Quat4f#mul(javax.vecmath.Quat4f, javax.vecmath.Quat4f)} gives
 * {@code this = q1 * q2}. {@link Quat4f#mul(javax.vecmath.Quat4f)} gives
 * {@code this = this * q}.
 */
public final class Quat4f extends javax.vecmath.Quat4f
{
	/**
	 * A quaternion that represents no rotation.
	 */
	public static final Quat4f IDENTITY = new Quat4f();
	
	
	public static Quat4f allocate() {
		return new Quat4f();
	}
	
	public static void release( Quat4f quat ) {
	
	}
	
	
	/**
	 * Construct a quaternion that represents no rotation. Init with
	 * {@code Quat4f{x=0.0F, y=0.0F, z=0.0F, w=1.0F}}.
	 */
	public Quat4f() {
		super( 0.0F, 0.0F, 0.0F, 1.0F );
	}
	
	public Quat4f( float x, float y, float z, float w ) {
		super( x, y, z, w );
	}
	
	public static Quat4f ofEulerRot( float pitch, float yaw, float roll )
	{
		final Quat4f quat = new Quat4f();
		quat.setEulerRot( pitch, yaw, roll );
		return quat;
	}
	
	public static Quat4f ofAxisAngle( AxisAngle4f axis_angle )
	{
		final Quat4f quat = new Quat4f();
		quat.set( axis_angle );
		return quat;
	}
	
	public static Quat4f rotOf( Mat4f mat )
	{
		final Quat4f quat = new Quat4f();
		quat.set( mat );
		return quat;
	}
	
	public void setEulerRot( float pitch, float yaw, float roll )
	{
		final Mat4f mat = Mat4f.allocate();
		mat.setIdentity();
		mat.eulerRotateYXZ( pitch, yaw, roll );
		this.set( mat );
		Mat4f.release( mat );
	}
	
	public void clearRot() {
		this.set( IDENTITY );
	}
	
	public void scaleAngle( float factor ) {
		this.interpolate( IDENTITY, this, factor );
	}
	
	/**
	 * Equivalent to first convert {@code scaled_axis} to quaternion form, and
	 * then {@code this = scaled_axis * this}.
	 *
	 * @see #addRot(Vec3f, float)
	 */
	public void addRot( Vec3f scaled_axis ) {
		this.addRot( scaled_axis, 1.0F );
	}
	
	/**
	 * @see #addRot(Vec3f)
	 */
	public void addRot( Vec3f angular_velocity, float time )
	{
		final float length = angular_velocity.length();
		final float radians = length * time * 0.5F;
		if ( radians < 10E-6 ) {
			return;
		}
		
		final Quat4f quat = allocate();
		final float sin = MathHelper.sin( radians );
		final float factor = sin / length;
		quat.w = MathHelper.cos( radians );
		quat.x = angular_velocity.x * factor;
		quat.y = angular_velocity.y * factor;
		quat.z = angular_velocity.z * factor;
		this.mul( quat, this );
		Quat4f.release( quat );
	}
	
	/**
	 * Apply rotation to a vector.
	 */
	public void transform( Vec3f point, Vec3f dst )
	{
		final Quat4f quat = allocate();
		quat.set( point.x, point.y, point.z, 0.0F );
		quat.mul( this, quat );
		quat.mulInverse( quat, this );  // TODO: Maybe use conjugate?
		dst.set( quat.x, quat.y, quat.z );
		Quat4f.release( quat );
	}
}
