package com.mcwb.util;

/**
 * A sub-type of {@link MotionTendency} that carries {@link #speedFactor}, {@link #maxForce} and
 * {@link #forceMult}
 * 
 * @author Giant_Salted_Fish
 */
public class BasedMotionTendency extends MotionTendency
{
	public float speedFactor;
	
	public float maxForce;
	
	public float forceMult;
	
	public BasedMotionTendency( float speedFactor, float maxForce, float forceMult )
	{
		this.speedFactor = speedFactor;
		this.maxForce = maxForce;
		this.forceMult = forceMult;
	}
	
	@Override
	public void update() { super.update( this.speedFactor, this.maxForce, this.forceMult ); }
	
	@Override
	public void update( float speedFactor, float maxForce, float forceMult )
	{
		super.update(
			this.speedFactor * speedFactor,
			this.maxForce * maxForce,
			this.forceMult * forceMult
		);
	}
}
