package com.mcwb.util;

import net.minecraft.util.math.MathHelper;

public class ArmTracker
{
	/**
	 * Recommended forearm and upper arm length. It is the typical value for standard steve model.
	 */
	public static final float RECOMMENDED_ARM_LENGTH = 10F / 16F;
	
	/**
	 * Length of forearm
	 */
	protected final float forearmLen;
	
	/**
	 * Length of upper arm
	 */
	protected final float upperArmLen;
	
	public final Vec3f shoulderPos = new Vec3f();
	
	public final Vec3f handPos = new Vec3f();
	
	public final Vec3f handRot = new Vec3f();
	
	public final Vec3f elbowPos = new Vec3f();
	
	public float armRotZ = 0F;
	
	protected final Mat4f mat = new Mat4f();
	protected final Vec3f vec = new Vec3f();
	
	/**
	 * Create an instance with forearm and upper arm length set to {@value #RECOMMENDED_ARM_LENGTH}
	 * 
	 * @see #RECOMMENDED_ARM_LENGTH
	 */
	public ArmTracker() { this( RECOMMENDED_ARM_LENGTH ); }
	
	public ArmTracker( float armLen ) { this( armLen, armLen ); }
	
	public ArmTracker( float forearmLen, float upperArmLen )
	{
		this.forearmLen = forearmLen;
		this.upperArmLen = upperArmLen;
	}
	
	public void setHandRotZ( float angle ) { this.handRot.z = angle; }
	
	/**
	 * Update current hand rotation and set previous rotation value. Note that x value of the
	 * hand rot will not be changed.
	 */
	public void updateArmOrientation()
	{
		final Mat4f mat = this.mat;
		final Vec3f vec = this.vec;
		
		// Get distance from hand to shoulder
		vec.set( this.handPos );
		vec.sub( this.shoulderPos );
		final float distanceSquared = vec.lengthSquared();
		final float distance = MathHelper.sqrt( distanceSquared );
		
		/// Get elbow coordinate
		// Case: distance is too short to organize a triangle
		if(
			this.forearmLen >= this.upperArmLen + distance
			|| this.upperArmLen >= this.forearmLen + distance
		) {
			if( !vec.nonZero() ) vec.set( 0F, -1F, 0F );
			else if( this.forearmLen > this.upperArmLen ) vec.negate();
			
			vec.scale( this.forearmLen / vec.length() );
			vec.add( this.handPos );
			this.elbowPos.add( vec );
		}
		// Case: distance it too long and arm is not enough
		else if( distance >= this.forearmLen + this.upperArmLen )
		{
			vec.negate();
			vec.scale( this.forearmLen / vec.length() );
			vec.add( this.handPos );
			this.elbowPos.set( vec );
		}
		// Case: normal triangular shape
		else
		{
			final float a = this.forearmLen;
			final float b = distance;
			final float a_2 = a * a;
			final float b_2 = distanceSquared;
			final float c_2 = this.upperArmLen * this.upperArmLen;
			final float cos = ( a_2 + b_2 - c_2 ) / ( 2F * a * b );
			final float sin = MathHelper.sqrt( 1F - cos * cos );
			this.elbowPos.set( 0F, -this.forearmLen * sin, distance - this.forearmLen * cos );
			
			// Get elbow coordinate in 3D space
			mat.setIdentity();
			vec.getEulerAngle( vec );
			mat.eulerRotateYXZ( vec.x, vec.y, this.armRotZ );
			mat.transformAsPoint( this.elbowPos );
			this.elbowPos.add( this.shoulderPos );
		}
		
		// Get hand angle
		vec.set( this.handPos );
		vec.sub( this.elbowPos );
		vec.getEulerAngle( this.handRot );
	}
}
