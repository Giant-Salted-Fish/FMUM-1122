package com.mcwb.util;

import net.minecraft.util.math.MathHelper;

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
	
	public void update() { this.update( 0.4F, 4.25F, 1F ); }
	
	public void update( float dampingFactor, float maxForce, float forceMult )
	{
		// Set previous value
		this.prevPos.set( this.curPos );
		
		this.velocity.scale( dampingFactor );
		
		this.vec.set( this.tarPos );
		this.vec.sub( this.curPos );
		
		// Make sure force is not exceeding max force
		float squared = this.vec.lengthSquared();
		if( squared > maxForce * maxForce )
			this.vec.scale( maxForce / MathHelper.sqrt( squared ) );
		
		// Apply modification on speed
		this.vec.scale( forceMult ); // TODO: move up ward
		this.velocity.add( this.vec );
		this.curPos.add( this.velocity );
	}
	
	/**
	 * Force current position to target position. When factor = 0, it simply keeps the current
	 * position unchanged. When factor = 1, the current position will be set to target position.
	 * 
	 * @param factor Force factor
	 */
	public final void approachTarPos( float factor )
	{
		this.vec.set( this.tarPos );
		this.vec.sub( this.curPos );
		this.vec.scale( factor );
		this.curPos.add( this.vec );
	}
	
	public final void getPos( Vec3f dst, float smoother )
	{
		dst.set( this.curPos );
		dst.sub( this.prevPos );
		dst.scale( smoother );
		dst.add( this.prevPos );
	}
	
	public final void applyPos( Vec3f dst, float smoother )
	{
		this.vec.set( this.curPos );
		this.vec.sub( this.prevPos );
		this.vec.scale( smoother );
		dst.add( this.vec );
		dst.add( this.prevPos );
	}
	
	public final float getX( float smoother )
	{
		float prev = this.prevPos.x;
		return prev + ( this.curPos.x - prev ) * smoother;
	}
	
	public final float getY( float smoother )
	{
		float prev = this.prevPos.y;
		return prev + ( this.curPos.y - prev ) * smoother;
	}
	
	public final float getZ( float smoother )
	{
		float prev = this.prevPos.z;
		return prev + ( this.curPos.z - prev ) * smoother;
	}
}
