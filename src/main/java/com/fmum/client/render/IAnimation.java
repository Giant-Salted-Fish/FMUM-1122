package com.fmum.client.render;

import com.fmum.util.Quat4f;
import com.fmum.util.Vec3f;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IAnimation extends IAnimator
{
	static final IAnimation NONE = new IAnimation()
	{
		@Override
		@SideOnly( Side.CLIENT )
		public void update() { }
		
		@Override
		@SideOnly( Side.CLIENT )
		public void getPos( String channel, Vec3f dst ) { dst.setZero(); }
		
		@Override
		@SideOnly( Side.CLIENT )
		public void getRot( String channel, Quat4f dst ) { dst.clearRot(); }
		
		@Override
		@SideOnly( Side.CLIENT )
		public float getFactor( String channel ) { return 0F; }
		
		@Override
		public String toString() { return "Animation::NONE"; }
	};
	
	@SideOnly( Side.CLIENT )
	void update();
}
