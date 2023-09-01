package com.fmum.client.animation;

import com.fmum.util.Quat4f;
import com.fmum.util.Vec3f;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public abstract class WrappedAnimator implements IAnimator
{
	private final IAnimator wrapped;
	
	protected WrappedAnimator( IAnimator wrapped ) {
		this.wrapped = wrapped;
	}
	
	@Override
	public void update() {
		this.wrapped.update();
	}
	
	@Override
	public void getPos( String channel, Vec3f dst ) {
		this.wrapped.getPos( channel, dst );
	}
	
	@Override
	public void getRot( String channel, Quat4f dst ) {
		this.wrapped.getRot( channel, dst );
	}
	
	@Override
	public float getFactor( String channel ) {
		return this.wrapped.getFactor( channel );
	}
	
	@Override
	public String toString() {
		return this.wrapped.toString();
	}
}
