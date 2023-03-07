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
	public void applyChannel( String channel, float smoother, Mat4f dst )
	{
		this.channels.computeIfPresent( channel, ( key, bone ) -> {
			dst.mul( bone.mat );
			return bone;
		} );
	}
	
	@Override
	public void applyChannel( String channel, float smoother, Quat4f dst )
	{
		this.channels.computeIfPresent( channel, ( key, bone ) -> {
			dst.mul( bone.quat );
			return bone;
		} );
	}
}
