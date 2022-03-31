package com.fmum.common.util;

public class ArmTendency
{
	/**
	 * Recommended forearm and upper arm length. It is the typical value for
	 * {@link com.fmum.client.model.ModelSteveArm} and {@link com.fmum.client.model.ModelAlexArm}.
	 */
	public static final double RECOMMENDED_ARM_LENGTH = 10D / 16D;
	
	protected static final CoordSystem sys = new CoordSystem();
	protected static final Vec3 vec = new Vec3();
	
	/**
	 * Length of upper arm
	 */
	public double upperArmLen;
	
	/**
	 * Length of forearm
	 */
	public double forearmLen;
	
	/**
	 * Track and simulate the motion of the hand and shoulder
	 */
	public final BasedMotionTendency
		shoulderPos = new BasedMotionTendency(0.4D, 0.125D, 0.25D),
		handPos = new BasedMotionTendency(0.4D, 0.125D, 0.25D),
		handRotX = new BasedMotionTendency(0.4D, 4.25D, 1D),
		armRotX = new BasedMotionTendency(0.4D, 4.25D, 1D);
	
	public final Vec3
		handRot = new Vec3(),
		prevHandRot = new Vec3();
	
//	public final Vec3 elbowPos = new Vec3();
	
	/**
	 * Create an instance with forearm and upper arm length set to {@value #RECOMMENDED_ARM_LENGTH}
	 * 
	 * @see #RECOMMENDED_ARM_LENGTH
	 */
	public ArmTendency() { this(RECOMMENDED_ARM_LENGTH); }
	
	public ArmTendency(double armLen) { this(armLen, armLen); }
	
	public ArmTendency(double upperArmLen, double forearmLen)
	{
		this.upperArmLen = upperArmLen;
		this.forearmLen = forearmLen;
	}
	
	public void setHandTarPos(Vec3 pos) { this.handPos.tarPos.set(pos); }
	
	public void setShoulderTarPos(Vec3 pos) { this.shoulderPos.tarPos.set(pos); }
	
	public void setHandTarRotX(double rotX) { this.handRotX.tarPos.x = rotX; }
	
	public void setArmTarRotX(double rotX) { this.armRotX.tarPos.x = rotX; }
	
	public void getSmoothedPos(Vec3 dest, float smoother) {
		this.handPos.getSmoothedPos(dest, smoother);
	}
	
	public void getSmoothedRot(Vec3 dest, double smoother)
	{
		dest.set(this.handRot);
		dest.sub(this.prevHandRot);
		dest.scale(smoother);
		dest.trans(this.prevHandRot);
		
		double prevX = this.handRotX.prevPos.x;
		dest.x = prevX + (this.handRotX.curPos.x - prevX) * smoother;
	}
	
	/**
	 * Default update. simply set all position and rotation to target value
	 */
	public void update()
	{
		this.shoulderPos.prevPos.set(this.shoulderPos.curPos);
		this.shoulderPos.approachTarPos(1D);
		this.handPos.prevPos.set(this.handPos.curPos);
		this.handPos.approachTarPos(1D);
		this.handRotX.prevPos.set(this.handRotX.curPos);
		this.handRotX.approachTarPos(1D);
		this.armRotX.prevPos.set(this.armRotX.curPos);
		this.armRotX.approachTarPos(1D);
		
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
		double disSquared = vec.lengthSquared();
		
		// Check side length before we calculate angle
		double distance = Math.sqrt(disSquared);
		if(
			this.forearmLen >= this.upperArmLen + distance
			|| this.upperArmLen >= this.forearmLen + distance
		) {
			// Distance is so small that we have to clip our arm
			this.handRot.y = -90D;
			this.handRot.z = 90D - this.armRotX.curPos.x;
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
		double rotY = vec.y, rotZ = vec.z;
		double cos = (
				this.forearmLen * this.forearmLen + disSquared
				- this.upperArmLen * this.upperArmLen
			) / (2 * this.forearmLen * distance);
		vec.set(
			distance - this.forearmLen * cos,
			-this.forearmLen * Math.sqrt(1D - cos * cos),
			0D
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
	public void setAbsoluteHandPos(Vec3 pos)
	{
		this.handPos.tarPos.set(pos);
		this.handPos.curPos.set(pos);
		this.handPos.prevPos.set(pos);
	}
	
	public void setAbsoluteShoulderPos(Vec3 pos)
	{
		this.shoulderPos.tarPos.set(pos);
		this.shoulderPos.curPos.set(pos);
		this.shoulderPos.prevPos.set(pos);
	}
	
	public void setAbsoluteHandRot(Vec3 rot)
	{
		this.handRot.set(rot);
		this.prevHandRot.set(rot);
	}
	
	public void setAbsoluteArmRot(double x)
	{
		this.armRotX.curPos.x
			= this.armRotX.tarPos.x
			= this.armRotX.prevPos.x
			= x;
	}
	/** for test */
}
