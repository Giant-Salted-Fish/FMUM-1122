package com.mcwb.util;

import net.minecraft.util.math.MathHelper;

/**
 * @author Giant_Salted_Fish
 */
public final class DynamicRot
{
	/**
	 * Target rotation that the system will attempt to get to
	 */
	public final Quat4f tarRot = new Quat4f( 0F, 0F, 0F, 1F );
	
	/**
	 * Current rotation of the system
	 */
	public final Quat4f curRot = new Quat4f( 0F, 0F, 0F, 1F );
	
	/**
	 * Rotation before last update
	 */
	public final Quat4f prevRot = new Quat4f( 0F, 0F, 0F, 1F );
	
	/**
	 * Current angular velocity of the system
	 */
	public final Quat4f velocity = new Quat4f( 0F, 0F, 0F, 1F );
	
	protected final Quat4f quat = new Quat4f();
	
	public void update( Vec3f forceMult, float maxForce, float dampingFactor )
	{
		this.prevRot.set( this.curRot );
		this.velocity.interpolate( Quat4f.ORIGIN, 1F - dampingFactor );
		
		final Quat4f deltaRot = this.quat;
		deltaRot.set( this.tarRot );
		deltaRot.mulInverse( this.curRot );
		
		// Actually should be twice of this angle
		final float deltaAngle = ( float ) Math.acos( deltaRot.w );
		if( deltaAngle > 0F )
		{
			final float ax = deltaRot.x;
			final float ay = deltaRot.y;
			final float az = deltaRot.z;
			final float oriLenSquared = ax * ax + ay * ay + az * az;
			
			final float fx = ax * forceMult.x;
			final float fy = ay * forceMult.y;
			final float fz = az * forceMult.z;
			final float lenSquared = fx * fx + fy * fy + fz * fz;
			
			final float rawForce = deltaAngle * MathHelper.sqrt( lenSquared / oriLenSquared );
			final float force = MathHelper.clamp( rawForce, -maxForce, maxForce );
			
			final float axisScale = MathHelper.sin( force ) / MathHelper.sqrt( oriLenSquared );
			deltaRot.x *= axisScale;
			deltaRot.y *= axisScale;
			deltaRot.z *= axisScale;
			deltaRot.w = MathHelper.cos( force );
			
			// Notice that order matters
			this.velocity.mul( deltaRot );
		}
		
		this.curRot.mul( this.velocity );
	}
	
//	public void update(
//		float forceMult,
//		float maxForce,
//		Vec3f inertia,
//		float dampingFactor
//	) {
//		final Quat4f tarRot = this.tarRot;
//		final Quat4f curRot = this.curRot;
//		this.prevRot.set( curRot );
//		
//		final Quat4f velocity = this.velocity;
//		velocity.interpolate( new Quat4f( 0F, 0F, 0F, 1F ), 1F - dampingFactor );
//		
//		// Make it from current rotate to target rotate
//		final Quat4f deltaRot = new Quat4f( tarRot );
//		deltaRot.mulInverse( curRot );
//		
//		// TODO: cos can not diff rot direction 
//		final float deltaAngle = ( float ) Math.acos( deltaRot.w );
//		final float angleForce = MathHelper.clamp( deltaAngle * forceMult, -maxForce, maxForce );
//		
//		final Vec3f rotAxis = new Vec3f( deltaRot.x, deltaRot.y, deltaRot.z );
//		final float oriLenSquared = rotAxis.lengthSquared();
//		if( deltaAngle > 0F )
//		{
//			final float x = rotAxis.x / inertia.x; // 可以和后面乘除对换
//			final float y = rotAxis.y / inertia.y;
//			final float z = rotAxis.z / inertia.z;
//			final float lenSquared = x * x + y * y + z * z;
//			
//			final float inertiaFactor = ( float ) Math.sqrt( lenSquared / oriLenSquared );
//			final float angleAcc = angleForce * inertiaFactor;
//			
//			final Quat4f rotAcc = new Quat4f();
//			rotAcc.set( new AxisAngle4f( rotAxis, angleAcc ) );
//			velocity.mul( rotAcc );
//		}
//		
//		curRot.mul( velocity );
//	}
	
	public void get( Quat4f dst, float smoother )
	{
		dst.set( this.prevRot );
		dst.interpolate( this.curRot, smoother );
	}
}
