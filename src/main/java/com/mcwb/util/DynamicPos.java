package com.mcwb.util;

import net.minecraft.util.math.MathHelper;

/**
 * This class simulates the movement of the object that connected to a spring in 3D space
 * 
 * @author Giant_Salted_Fish
 */
public final class DynamicPos
{
	/**
	 * Target position that the system will attempt to get to
	 */
	public final Vec3f tarPos = new Vec3f();
	
	/**
	 * Current position of the system
	 */
	public final Vec3f curPos = new Vec3f();
	
	/**
	 * Position before last update
	 */
	public final Vec3f prevPos = new Vec3f();
	
	/**
	 * Current velocity of the system
	 */
	public final Vec3f velocity = new Vec3f();
	
	/**
	 * A buffered vector to avoid frequent allocation on update for calculation
	 */
	protected final Vec3f vec = new Vec3f();
	
	public void update( float forceMult, float maxForce, float dampingFactor )
	{
		this.prevPos.set( this.curPos );
		this.velocity.scale( dampingFactor );
		
		// For spring we have: f=k*x
		final Vec3f force = this.vec;
		force.set( this.tarPos );
		force.sub( this.curPos );
		
		final float forceSquared = force.lengthSquared();
		if( forceSquared > maxForce * maxForce )
			force.scale( maxForce / MathHelper.sqrt( forceSquared ) );
		
		// Assume that mess=1 then acceleration equals force
		force.scale( forceMult );
		this.velocity.add( force );
		this.curPos.add( this.velocity );
	}
	
	/**
	 * Force current position to target position. When factor = 0, it simply keeps the current
	 * position unchanged. When factor = 1, the current position will be set to target position.
	 * 
	 * @param factor Force factor
	 */
	public void approachTarPos( float factor )
	{
		final Vec3f delta = this.vec;
		delta.set( this.tarPos );
		delta.sub( this.curPos );
		delta.scale( factor );
		
		this.curPos.add( delta );
	}
	
	public void get( Vec3f dst, float smoother )
	{
		dst.set( this.curPos );
		dst.sub( this.prevPos );
		dst.scale( smoother );
		dst.add( this.prevPos );
	}
	
	public float getX( float smoother )
	{
		float prev = this.prevPos.x;
		return prev + ( this.curPos.x - prev ) * smoother;
	}
	
	public float getY( float smoother )
	{
		float prev = this.prevPos.y;
		return prev + ( this.curPos.y - prev ) * smoother;
	}
	
	public float getZ( float smoother )
	{
		float prev = this.prevPos.z;
		return prev + ( this.curPos.z - prev ) * smoother;
	}
}
