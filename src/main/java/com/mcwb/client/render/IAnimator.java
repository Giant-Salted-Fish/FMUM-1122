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
		public void getPos( String channel, Vec3f dst ) { dst.setZero(); }
		
		@Override
		@SideOnly( Side.CLIENT )
		public void getRot( String channel, Quat4f dst ) { dst.clearRot(); }
		
		@Override
		public String toString() { return "Animator::NONE"; }
	};
	
	// TODO: add update for animation progress
//	@SideOnly( Side.CLIENT )
//	public default void update( float factor ) { }
	
	@SideOnly( Side.CLIENT )
	public void getPos( String channel, Vec3f dst );
	
	@SideOnly( Side.CLIENT )
	public void getRot( String channel, Quat4f dst );
	
	@SideOnly( Side.CLIENT )
	public default void getChannel( String channel, Mat4f dst )
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
	
	// Check if this is needed
	@SideOnly( Side.CLIENT )
	public default void applyChannel( String channel, Mat4f dst )
	{
		final Mat4f mat = Mat4f.locate();
		this.getChannel( channel, mat );
		dst.mul( mat );
		mat.release();
	}
	
	/**
	 * TODO: proper intro
	 * @see #blendPos(IAnimator, float, String, String, float, Mat4f)
	 * @see #blendRot(IAnimator, float, String, String, float, Mat4f)
	 * @return
	 *     Blend factor for animation. Usually {@code 0F} for static position and {@code 1F} for
	 *     animation.
	 */
	@SideOnly( Side.CLIENT )
	public default float getFactor( String channel ) { return 0F; }
	
//	@SideOnly( Side.CLIENT )
//	public static void blendPos(
//		IAnimator animator,
//		String channel0, String channel1, float alpha,
//		Mat4f dst
//	) {
//		final Vec3f v0 = Vec3f.locate();
//		final Vec3f v1 = Vec3f.locate();
//		
//		animator.getPos( channel0, v0 );
//		animator.getPos( channel1, v1 );
//		v0.interpolate( v1, alpha );
//		dst.translate( v0 );
//		
//		v1.release();
//		v0.release();
//	}
//	
//	@SideOnly( Side.CLIENT )
//	public static void blendRot(
//		IAnimator animator,
//		String channel0, String channel1, float alpha,
//		Mat4f dst
//	) {
//		final Quat4f q0 = Quat4f.locate();
//		final Quat4f q1 = Quat4f.locate();
//		
//		animator.getRot( channel0, q0 );
//		animator.getRot( channel1, q1 );
//		q0.interpolate( q1, alpha );
//		dst.rotate( q0 );
//		
//		q1.release();
//		q0.release();
//	}
}
