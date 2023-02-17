package com.oc.client.model;

import com.mcwb.client.gun.GripRenderer;
import com.mcwb.client.gun.GunAnimatorState;
import com.mcwb.client.gun.GunPartAnimatorState;
import com.mcwb.client.render.IAnimator;
import com.mcwb.common.gun.IGunPart;
import com.mcwb.common.pack.IContentProvider;
import com.mcwb.util.ArmTracker;
import com.mcwb.util.Vec3f;

import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class TDGripRenderer extends GripRenderer< IGunPart >
{
	public TDGripRenderer( String path, IContentProvider provider )
	{
		this.meshPaths = new String[] { "models/grip/td_grip.obj" };
		this.tbObjAdapt = true;
		this.scale = 1F / 160F;
		this.handPos = new Vec3f( 0F, 20F, 0F );
		
		this.build( path, provider );
	}
	
	@Override
	protected void doSetupArmToRender( ArmTracker arm, IAnimator animator )
	{
		final GunPartAnimatorState state = GunAnimatorState.INSTANCE;
		animator.getChannel( GunPartAnimatorState.CHANNEL_ITEM, this.smoother(), state.m0 );
		final float gunRotZ = state.m0.getEulerAngleZ();
		
		animator.getChannel( GunPartAnimatorState.CHANNEL_INSTALL, this.smoother(), state.m0 );
		final float installRotZ = state.m0.getEulerAngleZ();
//		leftArm.handPos.set( DevHelper.get( 0 ).getPos() );
//		leftArm.$handRotZ( gunRotZ + DevHelper.get( 0 ).getRot().z );
		
		arm.handPos.set( this.handPos );
		arm.$handRotZ( gunRotZ + ( installRotZ + 180F ) + this.handRotZ );
		
		final float hGripRot = installRotZ > -150F && installRotZ < -80F ? 35F + gunRotZ * 0.75F : 0F;
		final float armRot = this.armRotZ + hGripRot;
		arm.armRotZ = MathHelper.clamp( armRot, 0F, 90F );
		
		this.updateArm( arm, animator );
	}
}
