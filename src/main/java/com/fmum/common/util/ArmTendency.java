package com.fmum.common.util;

import com.fmum.common.util.MotionTendency.BasedMotionTendency;

public class ArmTendency
{
	protected static final CoordSystem sys = new CoordSystem();
	protected static final Vec3f vec = new Vec3f();
	
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
	public final BasedMotionTendency
		shoulderPos = new BasedMotionTendency(0.4F, 0.125F, 0.25F),
		handPos = new BasedMotionTendency(0.4F, 0.125F, 0.25F),
		handRotX = new BasedMotionTendency(0.4F, 4.25F, 1F),
		armRotX = new BasedMotionTendency(0.4F, 4.25F, 1F);
	
	public final Vec3f
		handRot = new Vec3f(),
		prevHandRot = new Vec3f();
	
//	public final Vector3f elbowPos = new Vector3f();
	
	public ArmTendency() { this(10F / 16F); }
	
	public ArmTendency(float armLen) { this(armLen, armLen); }
	
	public ArmTendency(float upperArmLen, float forearmLen)
	{
		this.upperArmLen = upperArmLen;
		this.forearmLen = forearmLen;
	}
	
	public void setHandTarPos(Vec3f pos) { this.handPos.tarPos.set(pos); }
	
	public void setShoulderTarPos(Vec3f pos) { this.shoulderPos.tarPos.set(pos); }
	
	public void setHandTarRotX(float rotX) { this.handRotX.tarPos.x = rotX; }
	
	public void setArmTarRotX(float rotX) { this.armRotX.tarPos.x = rotX; }
	
	public void getSmoothedPos(Vec3f dest, float smoother) {
		this.handPos.getSmoothedPos(dest, smoother);
	}
	
	public void getSmoothedRot(Vec3f dest, float smoother)
	{
		dest.set(this.handRot);
		dest.sub(this.prevHandRot);
		dest.scale(smoother);
		dest.trans(this.prevHandRot);
		
		float prevX = this.handRotX.prevPos.x;
		dest.x = prevX + (this.handRotX.curPos.x - prevX) * smoother;
	}
	
	/**
	 * Default update. simply set all position and rotation to target value
	 */
	public void update()
	{
		this.shoulderPos.prevPos.set(this.shoulderPos.curPos);
		this.shoulderPos.approachTarPos(1F);
		this.handPos.prevPos.set(this.handPos.curPos);
		this.handPos.approachTarPos(1F);
		this.handRotX.prevPos.set(this.handRotX.curPos);
		this.handRotX.approachTarPos(1F);
		this.armRotX.prevPos.set(this.armRotX.curPos);
		this.armRotX.approachTarPos(1F);
		
		this.updateArmOrientation();
	}
	
	/**
	 * Update current hand rotation and set previous rotation value. Note that x value of the
	 * hand rot will not be changed.
	 */
	public void updateArmOrientation()
	{
		// Update previous rotation value
		this.prevHandRot.y = this.handRot.y;
		this.prevHandRot.z = this.handRot.z;
		
		// Get distance from hand to shoulder
		vec.set(this.handPos.curPos);
		vec.sub(this.shoulderPos.curPos);
		float disSquared = vec.lengthSquared();
		
		// Check side length before we calculate angle
		float distance = (float)Math.sqrt(disSquared);
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
		sys.set(vec, CoordSystem.NORM_X);
		sys.getViewAngle(vec);
		
		// Arm is not long enough, set it straight
		if(distance >= this.forearmLen + this.upperArmLen)
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
			) / (2 * this.forearmLen * distance);
		vec.set(
			distance - this.forearmLen * cos,
			-this.forearmLen * (float)Math.sqrt(1F - cos * cos),
			0F
		);
		
		// Get elbow coordinate in 3D space
		sys.setDefault();
		sys.globalRot(this.armRotX.curPos.x, rotY, rotZ);
		sys.apply(vec, vec);
		vec.trans(this.shoulderPos.curPos);
		
//		this.elbowPos.set(vec); // for test
		
		// Get hand angle
		vec.sub(this.handPos.curPos);
		vec.negate();
		sys.set(vec, CoordSystem.X);
		sys.getViewAngle(this.handRot);
	}
	
	/** for test */
	public void setAbsoluteHandPos(Vec3f pos)
	{
		this.handPos.tarPos.set(pos);
		this.handPos.curPos.set(pos);
		this.handPos.prevPos.set(pos);
	}
	
	public void setAbsoluteShoulderPos(Vec3f pos)
	{
		this.shoulderPos.tarPos.set(pos);
		this.shoulderPos.curPos.set(pos);
		this.shoulderPos.prevPos.set(pos);
	}
	
	public void setAbsoluteHandRot(Vec3f rot)
	{
		this.handRot.set(rot);
		this.prevHandRot.set(rot);
	}
	
	public void setAbsoluteArmRot(float x)
	{
		this.armRotX.curPos.x
			= this.armRotX.tarPos.x
			= this.armRotX.prevPos.x
			= x;
	}
	/** for test */
}
