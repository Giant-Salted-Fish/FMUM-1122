package com.mcwb.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Root bone joint
 * 
 * @author Giant_SaltedF_Fish
 */
public class Animation
{
	public static final Animation NONE = new Animation()
	{
		@Override
		public void update( float progress ) { }
		
		@Override
		public void getPos( String channel, Vec3f dst ) { dst.setZero(); }
		
		@Override
		public void getRot( String channel, Quat4f dst ) { dst.clearRot(); }
		
		@Override
		public float getFactor( String channel ) { return 0F; }
	};
	
	public final Map< String, BoneAnimation > channels = new HashMap<>();
	
	/**
	 * Will call {@link BoneAnimation#update(float)} on these bones
	 */
	public final LinkedList< BoneAnimation > updateQueue = new LinkedList<>();
	// FIXME: make sure the bones all called in correct order in regard to their dependent relationship
	
	public void update( float progress ) { this.updateQueue.forEach( b -> b.update( progress ) ); }
	
	public void getPos( String channel, Vec3f dst )
	{
		final BoneAnimation ani = this.channels.get( channel );
		if( ani != null ) ani.mat.get( dst );
		else dst.setZero(); // TODO: use computeIfPresent maybe?
	}
	
	public void getRot( String channel, Quat4f dst )
	{
		final BoneAnimation ani = this.channels.get( channel );
		if( ani != null ) dst.set( ani.quat );
		else dst.clearRot();
	}
	
	public float getFactor( String channel )
	{
		final BoneAnimation ani = this.channels.get( channel );
		if( ani != null ) return ani.a;
		else return 1F;
	}
}
