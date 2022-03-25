package com.fmum.common.util;

/**
 * Tool class to help smooth motions in animation with sweet elastic effects
 * 
 * @author Giant_Salted_Fish
 */
public class MotionTendency
{
	public final Vec3f
		tarPos = new Vec3f(),
		curPos = new Vec3f(),
		prevPos = new Vec3f();
	
	public final Vec3f velocity = new Vec3f();
	
	/**
	 * Buffered vector for convenient vector operation
	 */
	protected final Vec3f vec = new Vec3f();
	
	public void update(float speedFactor, float maxForce, float forceMult)
	{
		// Set previous value
		this.prevPos.set(this.curPos);
		
		this.velocity.scale(speedFactor);
		
		this.vec.set(this.tarPos);
		this.vec.sub(this.curPos);
		
		// Make sure force is not exceeding max force
		float squared = vec.lengthSquared();
		if(squared > maxForce * maxForce)
			vec.scale(maxForce / (float)Math.sqrt(squared));
		
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
	public final void approachTarPos(float factor)
	{
		this.vec.set(this.tarPos);
		this.vec.sub(this.curPos);
		this.vec.scale(factor);
		this.curPos.trans(this.vec);
	}
	
	public final void getSmoothedPos(Vec3f dest, float smoother)
	{
		dest.set(this.curPos);
		dest.sub(this.prevPos);
		dest.scale(smoother);
		dest.trans(this.prevPos);
	}
	
	public final void applySmoothedPos(Vec3f dest, float smoother)
	{
		this.vec.set(this.curPos);
		this.vec.sub(this.prevPos);
		this.vec.scale(smoother);
		dest.trans(this.vec);
		dest.trans(this.prevPos);
	}
	
	public final float getSmoothedX(float smoother)
	{
		float prev = this.prevPos.x;
		return prev + (this.curPos.x - prev) * smoother;
	}
	
	public final float getSmoothedY(float smoother)
	{
		float prev = this.prevPos.y;
		return prev + (this.curPos.y - prev) * smoother;
	}
	
	public final float getSmoothedZ(float smoother)
	{
		float prev = this.prevPos.z;
		return prev + (this.curPos.z - prev) * smoother;
	}
	
	/**
	 * A sub-type of {@link MotionTendency} that carries {@link #speedFactor}, {@link #maxForce} and
	 * {@link #forceMult}
	 * 
	 * @author Giant_Salted_Fish
	 */
	public static class BasedMotionTendency extends MotionTendency
	{
		public float speedFactor;
		
		public float maxForce;
		
		public float forceMult;
		
		public BasedMotionTendency(float speedFactor, float maxForce, float forceMult)
		{
			this.speedFactor = speedFactor;
			this.maxForce = maxForce;
			this.forceMult = forceMult;
		}
		
		public void update() { super.update(this.speedFactor, this.maxForce, this.forceMult); }
		
		@Override
		public void update(float speedFactor, float maxForce, float forceMult)
		{
			super.update(
				this.speedFactor * speedFactor,
				this.maxForce * maxForce,
				this.forceMult * forceMult
			);
		}
	}
}
