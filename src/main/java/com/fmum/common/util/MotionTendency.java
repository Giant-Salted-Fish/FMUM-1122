package com.fmum.common.util;

/**
 * Tool class to help smooth motions in animation with sweet elastic effects
 * 
 * @author Giant_Salted_Fish
 */
public class MotionTendency
{
	public final Vec3
		tarPos = new Vec3(),
		curPos = new Vec3(),
		prevPos = new Vec3();
	
	public final Vec3 velocity = new Vec3();
	
	/**
	 * Buffered vector for convenient vector operation
	 */
	protected final Vec3 vec = new Vec3();
	
	public void update() { this.update(0.4D, 4.25D, 1D); }
	
	public void update(double speedFactor, double maxForce, double forceMult)
	{
		// Set previous value
		this.prevPos.set(this.curPos);
		
		this.velocity.scale(speedFactor);
		
		this.vec.set(this.tarPos);
		this.vec.sub(this.curPos);
		
		// Make sure force is not exceeding max force
		double squared = vec.lengthSquared();
		if(squared > maxForce * maxForce)
			vec.scale(maxForce / Math.sqrt(squared));
		
		// Apply modification on speed
		this.vec.scale(forceMult);
		this.velocity.trans(vec);
		this.curPos.trans(this.velocity);
	}
	
	/**
	 * Force current position to target position. When factor = 0, it simply keeps the current
	 * position unchanged. When factor = 1, the current position will be set to target position.
	 * 
	 * @param factor Force factor
	 */
	public final void approachTarPos(double factor)
	{
		this.vec.set(this.tarPos);
		this.vec.sub(this.curPos);
		this.vec.scale(factor);
		this.curPos.trans(this.vec);
	}
	
	public final void getSmoothedPos(Vec3 dst, float smoother)
	{
		dst.set(this.curPos);
		dst.sub(this.prevPos);
		dst.scale(smoother);
		dst.trans(this.prevPos);
	}
	
	public final void applySmoothedPos(Vec3 dst, float smoother)
	{
		this.vec.set(this.curPos);
		this.vec.sub(this.prevPos);
		this.vec.scale(smoother);
		dst.trans(this.vec);
		dst.trans(this.prevPos);
	}
	
	public final double getSmoothedX(double smoother)
	{
		double prev = this.prevPos.x;
		return prev + (this.curPos.x - prev) * smoother;
	}
	
	public final double getSmoothedY(double smoother)
	{
		double prev = this.prevPos.y;
		return prev + (this.curPos.y - prev) * smoother;
	}
	
	public final double getSmoothedZ(double smoother)
	{
		double prev = this.prevPos.z;
		return prev + (this.curPos.z - prev) * smoother;
	}
}
