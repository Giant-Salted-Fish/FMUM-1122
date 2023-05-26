package com.fmum.client.render;

import com.fmum.util.Quat4f;
import com.fmum.util.Vec3f;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public final class ReadOnlyAnimator implements IAnimator
{
	private final IAnimator animator;
	
	public ReadOnlyAnimator( IAnimator animator ) { this.animator = animator; }
	
	@Override
	public void update() { }
	
	@Override
	public void getPos( String channel, Vec3f dst ) { this.animator.getPos( channel, dst ); }
	
	@Override
	public void getRot( String channel, Quat4f dst ) { this.animator.getRot( channel, dst ); }
	
	@Override
	public float getFactor( String channel ) { return this.animator.getFactor( channel ); }
}
