package com.fmum.common.util;

/**
 * A sub-type of {@link MotionTendency} that carries {@link #speedFactor}, {@link #maxForce} and
 * {@link #forceMult}
 * 
 * @author Giant_Salted_Fish
 */
public class MotionTendencyBased extends MotionTendency
{
	public double speedFactor;
	
	public double maxForce;
	
	public double forceMult;
	
	public MotionTendencyBased(double speedFactor, double maxForce, double forceMult)
	{
		this.speedFactor = speedFactor;
		this.maxForce = maxForce;
		this.forceMult = forceMult;
	}
	
	@Override
	public void update() { super.update(this.speedFactor, this.maxForce, this.forceMult); }
	
	@Override
	public void update(double speedFactor, double maxForce, double forceMult)
	{
		super.update(
			this.speedFactor * speedFactor,
			this.maxForce * maxForce,
			this.forceMult * forceMult
		);
	}
}
