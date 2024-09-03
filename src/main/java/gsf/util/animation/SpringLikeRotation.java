package gsf.util.animation;

import gsf.util.math.Quat4f;
import gsf.util.math.Vec3f;

import javax.vecmath.Matrix3f;

/**
 * <p>Rotation version of the {@link MassSpringMotion}.</p>
 *
 * <p>A typical setup:
 * <ul>
 *     <li>{@code inverse_inertia={m00=0.025F, m11=0.015F, m22=0.005F}}</li>
 *     <li>{@code clamp_radians=MoreMath.PI/8}</li>
 *     <li>{@code damping=0.95F}</li>
 * </ul>
 * </p>
 */
public class SpringLikeRotation
{
	protected final Quat4f tar_rot = new Quat4f();
	protected final Quat4f cur_rot = new Quat4f();
	protected final Quat4f prev_rot = new Quat4f();
	
	/**
	 * Angular velocity in scaled-axis form.
	 */
	protected final Vec3f velocity = new Vec3f();
	
	public void update( Matrix3f inverse_inertia, float clamp_radians, float damping )
	{
		this.prev_rot.set( this.cur_rot );
		this.velocity.scale( damping );
		
		final Quat4f cur_offset = Quat4f.allocate();
		cur_offset.mulInverse( this.tar_rot, this.cur_rot );
		
		// Use the spring model: f=k*x.
		// Get torque axis and normalize it.
		final Vec3f axis = Vec3f.allocate();
		{
			axis.set( cur_offset.x, cur_offset.y, cur_offset.z );
			final float len = axis.length();  // MathHelper.sin( half_radians );
			if ( len < 10E-6 ) {
				axis.set( 1.0F, 0.0F, 0.0F );
			}
			else {
				axis.scale( 1.0F / len );
			}
		}
		
		// This gives angular acceleration axis and factor.
		inverse_inertia.transform( axis );
		this.cur_rot.transform( axis, axis );
		
		final float w = Math.min( 1.0F, cur_offset.w );  // Math.acos() will return Nan if w > 1.
		final float radians = 2.0F * ( float ) Math.acos( w );
		final float factor = Math.min( clamp_radians, radians );
		
		this.velocity.scaleAdd( factor, axis, this.velocity );
		this.cur_rot.addRot( this.velocity );
		
		Vec3f.release( axis );
		Quat4f.release( cur_offset );
	}
	
	public void getTarRot( Quat4f dst ) {
		dst.set( this.tar_rot );
	}
	
	public void setTarRot( Quat4f target_rot ) {
		this.tar_rot.set( target_rot );
	}
	
	public void getCurRot( Quat4f dst ) {
		dst.set( this.cur_rot );
	}
	
	public void getPreRot( Quat4f dst ) {
		dst.set( this.prev_rot );
	}
	
	public void resetRot( Quat4f rotation )
	{
		this.cur_rot.set( rotation );
		this.prev_rot.set( rotation );
	}
	
	/**
	 * Get the angular velocity in scaled-axis form.
	 */
	public void getVelocity( Vec3f dst ) {
		dst.set( this.velocity );
	}
	
	/**
	 * @param velocity The angular velocity in scaled-axis form.
	 */
	public void setVelocity( Vec3f velocity ) {
		this.velocity.set( velocity );
	}
	
	public void getRot( float alpha, Quat4f dst ) {
		dst.interpolate( this.prev_rot, this.cur_rot, alpha );
	}
}
