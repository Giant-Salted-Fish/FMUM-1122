package com.fmum.client.render;

import java.util.function.Supplier;

import com.fmum.util.Animation;
import com.fmum.util.Quat4f;
import com.fmum.util.Vec3f;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public final class CoupledAnimation implements IAnimator
{
	private final Animation animation;
	private final Supplier< Float > progress;
	
	public CoupledAnimation( Animation animation, Supplier< Float > progress )
	{
		this.animation = animation;
		this.progress = progress;
	}
	
	@Override
	public void update() { this.animation.update( this.progress.get() ); }
	
	@Override
	public void getPos( String channel, Vec3f dst ) { this.animation.getPos( channel, dst ); }
	
	@Override
	public void getRot( String channel, Quat4f dst ) { this.animation.getRot( channel, dst ); }
	
	@Override
	public float getFactor( String channel ) { return this.animation.getFactor( channel ); }
	
	@Override
	public String toString() { return this.animation.toString(); }
}
