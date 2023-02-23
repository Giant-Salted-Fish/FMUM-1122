//package com.mcwb.util;
//
//import net.minecraft.util.math.MathHelper;
//
///**
// * 很不幸我高估了自己的能力，我尝试将 DynamicPos 中的模型迁移应用到旋转当中并使用一些 tricky 的方法使其能够在四元数上工作，但
// * 结果并不如我预想的那样。物理知识储备仍有欠缺，待补充一些相关读物后再来尝试。暂时封存。
// * 
// * @author Giant_Salted_Fish
// */
//public final class DynamicRot
//{
//	/**
//	 * Target rotation that the system will attempt to get to
//	 */
//	public final Quat4f tarRot = new Quat4f( 0F, 0F, 0F, 1F );
//	
//	/**
//	 * Current rotation of the system
//	 */
//	public final Quat4f curRot = new Quat4f( 0F, 0F, 0F, 1F );
//	
//	/**
//	 * Rotation before last update
//	 */
//	public final Quat4f prevRot = new Quat4f( 0F, 0F, 0F, 1F );
//	
//	/**
//	 * Current angular velocity of the system
//	 */
//	public final Quat4f velocity = new Quat4f( 0F, 0F, 0F, 1F );
//	
//	protected final Quat4f quat = new Quat4f();
//	
//	public void update( Vec3f forceMult, float maxForce, float dampingFactor )
//	{
//		this.prevRot.set( this.curRot );
//		this.velocity.scaleAngle( dampingFactor );
//		
//		final Quat4f deltaRot = this.quat;
//		deltaRot.set( this.tarRot );
//		deltaRot.mulInverse( this.curRot );
//		
//		// Actually should be twice of this angle
//		final float deltaAngle = ( float ) Math.acos( deltaRot.w );
//		if( deltaAngle > 0F )
//		{
//			// This if statement actually can be removed by testing the divisor on divide. But \
//			// considering the work amount inside it, keep it here may be a better choice.
//			final float ax = deltaRot.x;
//			final float ay = deltaRot.y;
//			final float az = deltaRot.z;
//			final float oriLenSquared = ax * ax + ay * ay + az * az;
//			
//			final float fx = ax * forceMult.x;
//			final float fy = ay * forceMult.y;
//			final float fz = az * forceMult.z;
//			final float lenSquared = fx * fx + fy * fy + fz * fz;
//			
//			// Replace with {@code deltaAngle > 0F ? lenSquared / oriLenSquared : 0F} to avoid if
//			final float rawForce = Math.min( deltaAngle, maxForce );
//			final float force = rawForce * MathHelper.sqrt( lenSquared / oriLenSquared );
//			
//			final float axisScale = MathHelper.sin( force ) / MathHelper.sqrt( oriLenSquared );
//			deltaRot.x *= axisScale;
//			deltaRot.y *= axisScale;
//			deltaRot.z *= axisScale;
//			deltaRot.w = MathHelper.cos( force );
//			
//			// Notice that order matters
//			this.velocity.mul( deltaRot );
//		}
//		
//		this.curRot.mul( this.velocity );
//	}
//	
//	public void get( Quat4f dst, float smoother )
//	{
//		dst.set( this.prevRot );
//		dst.interpolate( this.curRot, smoother );
//	}
//}
