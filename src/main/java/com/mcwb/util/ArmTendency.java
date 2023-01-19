package com.mcwb.util;

import net.minecraft.util.math.MathHelper;

public class ArmTendency
{
	/**
	 * Recommended forearm and upper arm length. It is the typical value for
	 * {@link com.mcwb.client.model.ModelSteveArm} and {@link com.mcwb.client.model.ModelAlexArm}.
	 */
	public static final float RECOMMENDED_ARM_LENGTH = 10F / 16F;
	
	/**
	 * Length of upper arm
	 */
	public float upperArmLen;
	
	/**
	 * Length of forearm
	 */
	public float forearmLen;
	
	/**
	 * Track and simulate the motion of the hand and shoulder
	 */
	public final BasedMotionTendency // TODO: remove tendency
		shoulderPos = new BasedMotionTendency( 0.4F, 0.125F, 0.25F ),
		handPos = new BasedMotionTendency( 0.4F, 0.125F, 0.25F ),
		handRotX = new BasedMotionTendency( 0.4F, 4.25F, 1F ),
		armRotX = new BasedMotionTendency( 0.4F, 4.25F, 1F );
	
	public final Vec3f
		handRot = new Vec3f(),
		prevHandRot = new Vec3f();
	
	protected final Mat4f mat = new Mat4f();
	protected final Vec3f vec = new Vec3f();
	
//	public final Vec3 elbowPos = Vec3.get();
	
	/**
	 * Create an instance with forearm and upper arm length set to {@value #RECOMMENDED_ARM_LENGTH}
	 * 
	 * @see #RECOMMENDED_ARM_LENGTH
	 */
	public ArmTendency() { this( RECOMMENDED_ARM_LENGTH ); }
	
	public ArmTendency( float armLen ) { this( armLen, armLen ); }
	
	public ArmTendency( float upperArmLen, float forearmLen )
	{
		this.upperArmLen = upperArmLen;
		this.forearmLen = forearmLen;
	}
	
	public void setHandTarPos( Vec3f pos ) { this.handPos.tarPos.set( pos ); }
	
	public void setShoulderTarPos( Vec3f pos ) { this.shoulderPos.tarPos.set( pos ); }
	
	public void setHandTarRotX( float rotX ) { this.handRotX.tarPos.x = rotX; }
	
	public void setArmTarRotX( float rotX ) { this.armRotX.tarPos.x = rotX; }
	
	public void getSmoothedPos( Vec3f dst, float smoother ) {
		this.handPos.getPos( dst, smoother );
	}
	
	public void getSmoothedRot( Vec3f dst, float smoother )
	{
		dst.set( this.handRot );
		dst.subtract( this.prevHandRot );
		dst.scale( smoother );
		dst.translate( this.prevHandRot );
		
		float prevX = this.handRotX.prevPos.x;
		dst.x = prevX + ( this.handRotX.curPos.x - prevX ) * smoother;
	}
	
	/**
	 * Default update method. Simply set all position and rotation to target value.
	 */
	public void update()
	{
		this.shoulderPos.prevPos.set( this.shoulderPos.curPos );
		this.shoulderPos.approachTarPos( 1F );
		this.handPos.prevPos.set( this.handPos.curPos );
		this.handPos.approachTarPos( 1F );
		this.handRotX.prevPos.set( this.handRotX.curPos );
		this.handRotX.approachTarPos( 1F );
		this.armRotX.prevPos.set( this.armRotX.curPos );
		this.armRotX.approachTarPos( 1F );
		
		this.updateArmOrientation();
	}
	
	/**
	 * Update current hand rotation and set previous rotation value. Note that x value of the
	 * hand rot will not be changed.
	 */
	public void updateArmOrientation()
	{
		final Mat4f mat = this.mat;
		final Vec3f vec = this.vec;
		
		// Update previous rotation value
		this.prevHandRot.y = this.handRot.y;
		this.prevHandRot.z = this.handRot.z;
		
		// Get distance from hand to shoulder
		vec.set( this.handPos.curPos );
		vec.subtract( this.shoulderPos.curPos );
		float disSquared = vec.lengthSquared();
		
		// Check side length before we calculate angle
		float distance = MathHelper.sqrt( disSquared );
		if(
			this.forearmLen >= this.upperArmLen + distance
			|| this.upperArmLen >= this.forearmLen + distance
		) {
			// Distance is so small that we have to clip our arm
			this.handRot.y = -90F;
			this.handRot.z = 90F - this.armRotX.curPos.x;
			return;
		}
		
		// Get arm angle, it will be used later
		
		vec.getEulerAngle( vec );
		
		// Arm is not long enough, set it straight
		if( distance >= this.forearmLen + this.upperArmLen )
		{
			this.handRot.y = vec.y;
			this.handRot.z = vec.z;
			return;
		}
		
		// Get elbow coordinate
		float rotY = vec.y, rotZ = vec.z;
		float cos = (
			this.forearmLen * this.forearmLen + disSquared
			- this.upperArmLen * this.upperArmLen
		) / ( 2 * this.forearmLen * distance );
		vec.set(
			distance - this.forearmLen * cos,
			-this.forearmLen * MathHelper.sqrt( 1F - cos * cos ),
			0F
		);
		
		// Get elbow coordinate in 3D space
		mat.setIdentity();
		mat.eulerRotateYXZ( this.armRotX.curPos.x, rotY, rotZ );
		mat.apply( vec );
		vec.translate( this.shoulderPos.curPos );
		
//		this.elbowPos.set(vec); // for test
		
		// Get hand angle
		vec.subtract( this.handPos.curPos );
		vec.negate();
		vec.getEulerAngle( this.handRot );
	}
	
	/** for test 
	public void setAbsoluteHandPos( Vec3 pos )
	{
		this.handPos.tarPos.set( pos );
		this.handPos.curPos.set( pos );
		this.handPos.prevPos.set( pos );
	}
	
	public void setAbsoluteShoulderPos( Vec3 pos )
	{
		this.shoulderPos.tarPos.set( pos );
		this.shoulderPos.curPos.set( pos );
		this.shoulderPos.prevPos.set( pos );
	}
	
	public void setAbsoluteHandRot( Vec3 rot )
	{
		this.handRot.set( rot );
		this.prevHandRot.set( rot );
	}
	
	public void setAbsoluteArmRot( float x )
	{
		this.armRotX.curPos.x
			= this.armRotX.tarPos.x
			= this.armRotX.prevPos.x
			= x;
	}
	/** for test */
}
