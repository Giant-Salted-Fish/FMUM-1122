package com.mcwb.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

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
	 * {@link BoneAnimation#update(float)} will be called on these bones to start update.
	 */
	public final LinkedList< BoneAnimation > rootBones = new LinkedList<>();
	
	public void update( float progress ) { this.rootBones.forEach( b -> b.update( progress ) ); }
	
	public void getPos( String channel, Vec3f dst )
	{
		final BoneAnimation ani = this.channels.get( channel );
		if ( ani != null ) { ani.mat.get( dst ); }
		else { dst.setZero(); } // TODO: use computeIfPresent maybe?
	}
	
	public void getRot( String channel, Quat4f dst )
	{
		final BoneAnimation ani = this.channels.get( channel );
		if ( ani != null ) { dst.set( ani.quat ); }
		else { dst.clearRot(); }
	}
	
	public float getFactor( String channel )
	{
		final BoneAnimation ani = this.channels.get( channel );
		if ( ani != null ) { return ani.a; }
		else { return 1F; }
	}
}
