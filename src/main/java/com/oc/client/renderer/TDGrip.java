package com.oc.client.renderer;

import com.mcwb.client.gun.GripRenderer;
import com.mcwb.client.render.IAnimator;
import com.mcwb.common.gun.IGunPart;
import com.mcwb.common.pack.IContentProvider;
import com.mcwb.util.ArmTracker;
import com.mcwb.util.Mat4f;
import com.mcwb.util.Vec3f;

import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class TDGrip extends GripRenderer< IGunPart >
{
	public TDGrip( String path, IContentProvider provider )
	{
		this.meshPaths = new String[] { "renderers/grip/td_grip.obj" };
		this.tbObjAdapt = true;
		this.scale = 1F / 160F;
		this.handPos = new Vec3f( 0F, 20F, 0F );
		
		this.build( path, provider );
	}
	
	@Override
	protected void doSetupArmToRender( ArmTracker arm, IAnimator animator )
	{
		final Mat4f mat = Mat4f.locate();
		animator.getChannel( CHANNEL_ITEM, this.smoother(), mat );
		final float gunRotZ = mat.getEulerAngleZ();
		
		animator.getChannel( CHANNEL_INSTALL, this.smoother(), mat );
		final float installRotZ = mat.getEulerAngleZ();
		mat.release();
//		leftArm.handPos.set( DevHelper.get( 0 ).getPos() );
//		leftArm.$handRotZ( gunRotZ + DevHelper.get( 0 ).getRot().z );
		
		arm.handPos.set( this.handPos );
		arm.$handRotZ( gunRotZ + ( installRotZ + 180F ) + this.handRotZ );
		
		final boolean isTilt = installRotZ > -150F && installRotZ < -80F;
		final float armRot = this.armRotZ + ( isTilt ? 35F + gunRotZ * 0.75F : 0F );
		arm.armRotZ = MathHelper.clamp( armRot, 0F, 90F );
		
		this.updateArm( arm, animator );
	}
}
