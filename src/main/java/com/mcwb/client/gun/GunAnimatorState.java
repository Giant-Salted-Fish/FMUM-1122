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
	
	// Hack way to get pos and rot from super
	protected final IAnimator superAnimator = new IAnimator()
	{
		@Override
		public void getPos( String channel, Vec3f dst ) {
			GunAnimatorState.super.getPos( channel, dst );
		}
		
		@Override
		public void getRot( String channel, Quat4f dst ) {
			GunAnimatorState.super.getRot( channel, dst );
		}
	};
	
	public GunAnimatorState()
	{
		this.leftArm.shoulderPos.set( 5F / 16F, -4F / 16F, 0F );
		this.rightArm.shoulderPos.set( -3F / 16F, -4F / 16F, -2.5F / 16F );
	}
	
	@Override
	public void getPos( String channel, Vec3f dst )
	{
		switch( channel )
		{
		case IItemRenderer.CHANNEL_ITEM:
			{
				final Mat4f mat = Mat4f.locate();
				IAnimator.getChannel( this.superAnimator, channel, mat );
				IAnimator.applyChannel( this.animtion, IGunRenderer.CHANNEL_GUN, mat );
				mat.get( dst );
				mat.release();
			}
			break;
			
		case IGunRenderer.CHANNEL_GUN:
		case IMagRenderer.CHANNEL_MAG:
		case IMagRenderer.CHANNEL_LOADING_MAG:
			this.animtion.getPos( channel, dst );
			break;
			
		case IGunRenderer.CHANNEL_LEFT_ARM:
			{
				// TODO: maybe left gun render to blend the animation
				this.animtion.getPos( channel, dst );
				
				final Mat4f mat = Mat4f.locate();
				IAnimator.getChannel( this, IItemRenderer.CHANNEL_ITEM, mat );
				mat.transformAsPoint( dst );
				mat.release();
				
				final float alpha = this.animtion.getFactor( channel );
				dst.interpolate( this.leftArm.handPos, 1F - alpha );
			}
			break;
			
		case IGunRenderer.CHANNEL_RIGHT_ARM:
			{
				this.animtion.getPos( channel, dst );
				
				final Mat4f mat = Mat4f.locate();
				IAnimator.getChannel( this, IItemRenderer.CHANNEL_ITEM, mat );
				mat.transformAsPoint( dst );
				mat.release();
				
				final float alpha = this.animtion.getFactor( channel );
				dst.interpolate( this.rightArm.handPos, 1F - alpha );
			}
			break;
			
		default: dst.setZero();
		}
	}
	
	@Override
	public void getRot( String channel, Quat4f dst )
	{
		switch( channel )
		{
		case IItemRenderer.CHANNEL_ITEM:
			{
				super.getRot( channel, dst );
				
				final Quat4f quat = Quat4f.locate();
				this.animtion.getRot( IGunRenderer.CHANNEL_GUN, quat );
				dst.mul( quat );
				quat.release();
			}
			break;
			
		case IGunRenderer.CHANNEL_GUN:
		case IMagRenderer.CHANNEL_MAG:
		case IMagRenderer.CHANNEL_LOADING_MAG:
			this.animtion.getRot( channel, dst );
			break;
			
		case IGunRenderer.CHANNEL_LEFT_ARM:
			{
				this.getRot( IItemRenderer.CHANNEL_ITEM, dst );
				
				// TODO: re-check the order of quat mul
				final Quat4f quat = Quat4f.locate();
				this.animtion.getRot( channel, quat );
				dst.mul( quat );
				
				final float alpha = this.animtion.getFactor( channel );
				quat.set( this.leftArm.handRot );
				dst.interpolate( quat, 1F - alpha );
				quat.release();
			}
			break;
			
		case IGunRenderer.CHANNEL_RIGHT_ARM:
			{
				this.getRot( IItemRenderer.CHANNEL_ITEM, dst );
				
				final Quat4f quat = Quat4f.locate();
				this.animtion.getRot( channel, quat );
				dst.mul( quat );
				
				final float alpha = this.animtion.getFactor( channel );
				quat.set( this.rightArm.handRot );
				dst.interpolate( quat, 1F - alpha );
				quat.release();
			}
			break;
			
		default: dst.clearRot();
		}
	}
	
	@Override
	public float getFactor( String channel ) { return this.animtion.getFactor( channel ); }
}
