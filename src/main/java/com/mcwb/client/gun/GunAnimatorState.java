package com.mcwb.client.gun;

import com.mcwb.client.item.IItemRenderer;
import com.mcwb.client.render.IAnimator;
import com.mcwb.util.ArmTracker;
import com.mcwb.util.Mat4f;
import com.mcwb.util.Quat4f;
import com.mcwb.util.Vec3f;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class GunAnimatorState extends GunPartAnimatorState
{
	public static final GunAnimatorState INSTANCE = new GunAnimatorState();
	
	public static float dropDistanceCycle = 0F;
	public static float walkDistanceCycle = 0F;
	
	public static float prevPlayerPitch = 0F;
	public static float prevPlayerYaw = 0F;
	
	public final ArmTracker leftArm = new ArmTracker();
	public final ArmTracker rightArm = new ArmTracker();
	
	public IAnimator animtion = IAnimator.INSTANCE;
	
	// Hack way to get orientation from super
	protected final IAnimator superAnimator = new IAnimator()
	{
		@Override
		public void getRot( String channel, float smoother, Quat4f dst ) {
			GunAnimatorState.super.getRot( channel, smoother, dst );
		}
		
		@Override
		public void getPos( String channel, float smoother, Vec3f dst ) {
			GunAnimatorState.super.getPos( channel, smoother, dst );
		}
	};
	
	public GunAnimatorState()
	{
		this.leftArm.shoulderPos.set( 5F / 16F, -4F / 16F, 0F );
		this.rightArm.shoulderPos.set( -3F / 16F, -4F / 16F, -2.5F / 16F );
	}
	
	@Override
	public void getPos( String channel, float smoother, Vec3f dst )
	{
		switch( channel )
		{
		case IItemRenderer.CHANNEL_ITEM:
			{
				final Mat4f mat = Mat4f.locate();
				IAnimator.getChannel( this.superAnimator, channel, smoother, mat );
				IAnimator.applyChannel( this.animtion, channel, smoother, mat );
				mat.get( dst );
				mat.release();
			}
			break;
			
		case IMagRenderer.CHANNEL_MAG:
			this.animtion.getPos( channel, smoother, dst );
			break;
			
		case IGunRenderer.CHANNEL_LEFT_ARM:
			{
				// TODO: maybe left gun render to blend the animation
				this.animtion.getPos( channel, smoother, dst );
				
				final Mat4f mat = Mat4f.locate();
				IAnimator.getChannel( this, IItemRenderer.CHANNEL_ITEM, smoother, mat );
				mat.transformAsPoint( dst );
				mat.release();
				
				final float alpha = this.animtion.getAlpha( channel, smoother );
				dst.interpolate( this.leftArm.handPos, 1F - alpha );
			}
			break;
			
		case IGunRenderer.CHANNEL_RIGHT_ARM:
			{
				this.animtion.getPos( channel, smoother, dst );
				
				final Mat4f mat = Mat4f.locate();
				IAnimator.getChannel( this, IItemRenderer.CHANNEL_ITEM, smoother, mat );
				mat.transformAsPoint( dst );
				mat.release();
				
				final float alpha = this.animtion.getAlpha( channel, smoother );
				dst.interpolate( this.rightArm.handPos, 1F - alpha );
			}
			break;
			
		default: dst.setZero();
		}
	}
	
	@Override
	public void getRot( String channel, float smoother, Quat4f dst )
	{
		switch( channel )
		{
		case IItemRenderer.CHANNEL_ITEM:
			{
				final Quat4f quat = Quat4f.locate();
				super.getRot( channel, smoother, dst );
				this.animtion.getRot( channel, smoother, quat );
				dst.mul( quat );
				quat.release();
			}
			break;
			
		case IMagRenderer.CHANNEL_MAG:
			this.animtion.getRot( channel, smoother, dst );
			break;
			
		case IGunRenderer.CHANNEL_LEFT_ARM:
			{
				// TODO: re-check the order of quat mul
				final float alpha = this.animtion.getAlpha( channel, smoother );
				final Quat4f quat = Quat4f.locate();
				this.getRot( IItemRenderer.CHANNEL_ITEM, smoother, dst );
				this.animtion.getRot( channel, smoother, quat );
				dst.mul( quat );
				
				quat.set( this.leftArm.handRot );
				dst.interpolate( quat, 1F - alpha );
				quat.release();
			}
			break;
			
		case IGunRenderer.CHANNEL_RIGHT_ARM:
			{
				final float alpha = this.animtion.getAlpha( channel, smoother );
				final Quat4f quat = Quat4f.locate();
				this.getRot( IItemRenderer.CHANNEL_ITEM, smoother, dst );
				this.animtion.getRot( channel, smoother, quat );
				dst.mul( quat );
				
				quat.set( this.rightArm.handRot );
				dst.interpolate( quat, 1F - alpha );
				quat.release();
			}
			break;
			
		default: dst.clearRot();
		}
	}
	
	@Override
	public float getAlpha( String channel, float smoother ) {
		return this.animtion.getAlpha( channel, smoother );
	}
}
