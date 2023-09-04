package com.fmum.client.animation;

import com.fmum.util.Mat4f;
import com.fmum.util.Quat4f;
import com.fmum.util.Vec3f;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public interface IAnimator
{
	String ABSENT_CHANNEL = "";
	
	IAnimator NONE = new IAnimator()
	{
		@Override
		public void getPos( String channel, Vec3f dst ) {
			dst.setZero();
		}
		
		@Override
		public void getRot( String channel, Quat4f dst ) {
			dst.clearRot();
		}
		
		@Override
		public float getFactor( String channel ) {
			return 0.0F;
		}
	};
	
	default void update() { }
	
	void getPos( String channel, Vec3f dst );
	
	void getRot( String channel, Quat4f dst );
	
	float getFactor( String channel );
	
	default void getChannel( String channel, Mat4f dst )
	{
		dst.setIdentity();
		
		final Vec3f vec = Vec3f.locate();
		this.getPos( channel, vec );
		dst.translate( vec );
		vec.release();
		
		final Quat4f quat = Quat4f.locate();
		this.getRot( channel, quat );
		dst.rotate( quat );
		quat.release();
	}
	
	default void applyChannel( String channel, Mat4f dst )
	{
		final Mat4f mat = Mat4f.locate();
		this.getChannel( channel, mat );
		dst.mul( mat );
		mat.release();
	}
}
