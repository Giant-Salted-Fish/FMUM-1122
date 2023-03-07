package com.mcwb.util;

import java.util.HashMap;
import java.util.Map;

import com.mcwb.client.render.IAnimator;

/**
 * Root bone joint
 * 
 * @author Giant_SaltedF_Fish
 */
public class Animation extends BoneAnimation implements IAnimator
{
	public final Map< String, BoneAnimation > channels = new HashMap<>();
	
	public Animation( String channel ) { this.channels.put( channel, this ); }
	
	@Override
	public void getPos( String channel, float smoother, Vec3f dst )
	{
		final BoneAnimation ani = this.channels.get( channel );
		if( ani != null ) ani.mat.get( dst );
		else dst.setZero(); // TODO: use computeIfPresent maybe?
	}
	
	@Override
	public void getRot( String channel, float smoother, Quat4f dst )
	{
		final BoneAnimation ani = this.channels.get( channel );
		if( ani != null ) dst.set( ani.quat );
		else dst.clearRot();
	}
	
	@Override
	public float getAlpha( String channel, float smoother )
	{
		final BoneAnimation ani = this.channels.get( channel );
		if( ani != null ) return ani.a;
		else return 1F;
	}
}
