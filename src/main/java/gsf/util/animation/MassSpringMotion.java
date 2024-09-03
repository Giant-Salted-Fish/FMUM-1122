package gsf.util.animation;

import gsf.util.math.MoreMath;
import gsf.util.math.Vec3f;

/**
 * <p>Simulates the motion of a point mass constrained by a spring.</p>
 *
 * <p>A typical setup:
 * <ul>
 *     <li>{@code spring_k=1.0F}</li>
 *     <li>{@code distance_clamp=4.25F}</li>
 *     <li>{@code damping=0.4F}</li>
 * </ul>
 * </p>
 *
 * @see SpringLikeRotation
 */
public class MassSpringMotion
{
	protected final Vec3f tar_pos = new Vec3f();
	protected final Vec3f cur_offset = new Vec3f();
	protected final Vec3f prev_offset = new Vec3f();
	// {Current Pos} = {Target Pos} + {Current Offset}.
	
	protected final Vec3f velocity = new Vec3f();
	
	public void update( float spring_k, float distance_clamp, float damping )
	{
		final Vec3f cur_offset = this.cur_offset;
		this.prev_offset.set( cur_offset );
		this.velocity.scale( damping );
		
		// Use the spring model: f=k*x.
		final float dis_p2 = cur_offset.lengthSquared();
		final float clamp_p2 = distance_clamp * distance_clamp;
		final float factor = (
			dis_p2 > clamp_p2
			? distance_clamp * MoreMath.fastInvSqrt( dis_p2 )
			: 1.0F
		);
		
		// Assume that mess=1, then acceleration equals force.
		this.velocity.scaleAdd( -factor * spring_k, cur_offset, this.velocity );
		cur_offset.add( this.velocity );
	}
	
	public void getTarPos( Vec3f dst ) {
		dst.set( this.tar_pos );
	}
	
	public void setTarPos( Vec3f target_pos )
	{
		this.cur_offset.add( this.tar_pos );
		this.cur_offset.sub( target_pos );
		this.tar_pos.set( target_pos );
	}
	
	public void getCurPos( Vec3f dst )
	{
		dst.set( this.tar_pos );
		dst.add( this.cur_offset );
	}
	
	public void getPrevPos( Vec3f dst )
	{
		dst.set( this.tar_pos );
		dst.add( this.prev_offset );
	}
	
	public void resetPos( Vec3f position )
	{
		this.cur_offset.set( position );
		this.cur_offset.sub( this.tar_pos );
		this.prev_offset.set( this.cur_offset );
	}
	
	public void getVelocity( Vec3f dst ) {
		dst.set( this.velocity );
	}
	
	public void setVelocity( Vec3f velocity ) {
		this.velocity.set( velocity );
	}
	
	public void offsetVelocity( float x, float y, float z ) {
		this.velocity.add( x, y, z );
	}
	
	public void getPos( float alpha, Vec3f dst )
	{
		dst.interpolate( this.prev_offset, this.cur_offset, alpha );
		dst.add( this.tar_pos );
	}
	
	public float getPosX( float alpha )
	{
		final float offset = MoreMath.lerp( this.prev_offset.x, this.cur_offset.x, alpha );
		return this.tar_pos.x + offset;
	}
	
	public float getPosY( float alpha )
	{
		final float offset = MoreMath.lerp( this.prev_offset.y, this.cur_offset.y, alpha );
		return this.tar_pos.y + offset;
	}
	
	public float getPosZ( float alpha )
	{
		final float offset = MoreMath.lerp( this.prev_offset.z, this.cur_offset.z, alpha );
		return this.tar_pos.z + offset;
	}
}
