package com.mcwb.client.render;

import com.mcwb.util.Mat4f;
import com.mcwb.util.Quat4f;
import com.mcwb.util.Vec3f;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @see IRenderer
 * @author Giant_Salted_Fish
 */
public interface IAnimator
{
	public static final IAnimator INSTANCE = new IAnimator()
	{
		@Override
		@SideOnly( Side.CLIENT )
		public void getPos( String channel, float smoother, Vec3f dst ) { dst.setZero(); }
		
		@Override
		@SideOnly( Side.CLIENT )
		public void getRot( String channel, float smoother, Quat4f dst ) { dst.clearRot(); }
	};
	
	@SideOnly( Side.CLIENT )
	public default void update( float progress ) { }
	
	@SideOnly( Side.CLIENT )
	public void getPos( String channel, float smoother, Vec3f dst );
	
	@SideOnly( Side.CLIENT )
	public void getRot( String channel, float smoother, Quat4f dst );
	
	/**
	 * @see #blendPos(IAnimator, float, String, String, float, Mat4f)
	 * @see #blendRot(IAnimator, float, String, String, float, Mat4f)
	 * @return
	 *     Blend factor for animation. Usually {@code 0F} for static position and {@code 1F} for
	 *     animation.
	 */
	@SideOnly( Side.CLIENT )
	public default float getAlpha( String channel, float smoother ) { return 0F; }
	
	// TODO: check if it is needed to remove this
	@SideOnly( Side.CLIENT )
	public static void getChannel( IAnimator animator, String channel, float smoother, Mat4f dst )
	{
		dst.setIdentity();
		applyChannel( animator, channel, smoother, dst );
	}
	
	@SideOnly( Side.CLIENT )
	public static void applyChannel( IAnimator animator, String channel, float smoother, Mat4f dst )
	{
		final Vec3f vec = Vec3f.locate();
		animator.getPos( channel, smoother, vec );
		dst.translate( vec );
		vec.release();
		
		final Quat4f quat = Quat4f.locate();
		animator.getRot( channel, smoother, quat );
		dst.rotate( quat );
		quat.release();
	}
	
	@SideOnly( Side.CLIENT )
	public static void blendPos(
		IAnimator animator, float smoother,
		String channel0, String channel1, float alpha,
		Mat4f dst
	) {
		final Vec3f v0 = Vec3f.locate();
		final Vec3f v1 = Vec3f.locate();
		
		animator.getPos( channel0, smoother, v0 );
		animator.getPos( channel1, smoother, v1 );
		v0.interpolate( v1, alpha );
		dst.translate( v0 );
		
		v1.release();
		v0.release();
	}
	
	@SideOnly( Side.CLIENT )
	public static void blendRot(
		IAnimator animator, float smoother,
		String channel0, String channel1, float alpha,
		Mat4f dst
	) {
		final Quat4f q0 = Quat4f.locate();
		final Quat4f q1 = Quat4f.locate();
		
		animator.getRot( channel0, smoother, q0 );
		animator.getRot( channel1, smoother, q1 );
		q0.interpolate( q1, alpha );
		dst.rotate( q0 );
		
		q1.release();
		q0.release();
	}
}
