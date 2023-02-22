package com.mcwb.util;

/**
 * A sub-type of {@link MotionTendency} that carries {@link #dampingFactor}, {@link #maxForce} and
 * {@link #forceMult}
 * 
 * @author Giant_Salted_Fish
 */
public class BasedMotionTendency extends MotionTendency
{
	public float dampingFactor;
	
	public float maxForce;
	
	public float forceMult;
	
	public BasedMotionTendency( float dampingFactor, float maxForce, float forceMult )
	{
		this.dampingFactor = dampingFactor;
		this.maxForce = maxForce;
		this.forceMult = forceMult;
	}
	
	@Override
	public void update() { super.update( this.dampingFactor, this.maxForce, this.forceMult ); }
	
	@Override
	public void update( float dampingFactor, float maxForce, float forceMult )
	{
		super.update(
			this.dampingFactor * dampingFactor,
			this.maxForce * maxForce,
			this.forceMult * forceMult
		);
	}
}
