package com.mcwb.client.item;

import com.mcwb.client.render.IAnimator;
import com.mcwb.util.Quat4f;
import com.mcwb.util.Vec3f;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class ModifiableItemAnimatorState extends ItemAnimatorState
{
//	public OpModifyClient modifyOp = ModifiableItemType.OP_MODIFY;
	
	public Vec3f modifyPos = Vec3f.ORIGIN;
	
	public IAnimator animtion = IAnimator.INSTANCE;
	
	@Override
	public void getPos( String channel, Vec3f dst )
	{
		switch( channel )
		{
		case IItemRenderer.CHANNEL_ITEM:
//		case IModuleRenderer.CHANNEL_MODULE:
			// Reserved for modify operation
			this.animtion.getPos( IItemRenderer.CHANNEL_ITEM, dst );
			dst.interpolate( this.pos, 1F - this.animtion.getFactor( IItemRenderer.CHANNEL_ITEM ) );
			break;
			
			// TODO: move this part to Animation
//			final float modifyProgress = this.modifyOp.getProgress( smoother );
//			
//			this.holdPos.get( dst, smoother );
//			dst.scale( 1F - modifyProgress );
//			
//			final Vec3f vec = Vec3f.locate();
//			vec.set( this.modifyPos );
//			vec.scale( modifyProgress );
//			dst.add( vec );
//			vec.release();
//			break;
			
		default: dst.setZero();
		}
	}
	
	@Override
	public void getRot( String channel, Quat4f dst )
	{
		switch( channel )
		{
		case IItemRenderer.CHANNEL_ITEM:
//		case IModuleRenderer.CHANNEL_MODULE:
			this.animtion.getRot( IItemRenderer.CHANNEL_ITEM, dst );
			dst.interpolate( this.rot, 1F - this.animtion.getFactor( IItemRenderer.CHANNEL_ITEM ) );
			break;
			
			// TODO: move this part to Animation
//			final float modifyProgress = this.modifyOp.getProgress( smoother );
//			
//			final Mat4f mat = Mat4f.locate();
//			final Vec3f vec = Vec3f.locate();
//			
//			this.holdRot.get( vec, smoother );
//			vec.scale( 1F - modifyProgress );
//			
//			// TODO: player#rotate seems to have lag when being used to render 
//			final EntityPlayerSP player = MCWBClient.MC.player;
//			final float refPlayerYaw = this.modifyOp.refPlayerRotYaw;
//			final float modifyYawBase = ( refPlayerYaw % 360F + 360F ) % 360F - 180F; // TODO: maybe do this when capture ref player yaw
//			final float modifyYawDelta = refPlayerYaw - player.rotationYaw; // TODO: get this from camera controller
//			final float modifyYaw = modifyYawBase - modifyYawDelta;
//			
//			mat.setIdentity();
//			mat.rotateX( -player.rotationPitch * modifyProgress );
//			mat.eulerRotateYXZ( vec.x, vec.y + modifyYaw * modifyProgress, vec.z );
//			dst.set( mat );
//			
//			vec.release();
//			mat.release();
//			break;
			
		default: dst.clearRot();
		}
	}
}
