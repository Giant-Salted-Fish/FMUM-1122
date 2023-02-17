package com.mcwb.client.gun;

import com.mcwb.client.render.IAnimator;
import com.mcwb.client.render.IRenderer;
import com.mcwb.common.gun.IGunPart;
import com.mcwb.common.load.BuildableLoader;
import com.mcwb.devtool.DevHelper;
import com.mcwb.util.ArmTracker;

import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class CarGripRenderer< T extends IGunPart > extends GripRenderer< T >
{
	public static final BuildableLoader< IRenderer >
		LOADER = new BuildableLoader<>( "car_grip", CarGripRenderer.class );
	
	protected float armRotGunFactor = 1F;
	
	@Override
	protected void doSetupArmToRender( ArmTracker arm, IAnimator animator )
	{
		final GunPartAnimatorState state = GunAnimatorState.INSTANCE;
		animator.getChannel( GunPartAnimatorState.CHANNEL_ITEM, this.smoother(), state.m0 );
		final float gunRotZ = state.m0.getEulerAngleZ();
		
//		leftArm.handPos.set( DevHelper.get( 0 ).getPos() );
//		leftArm.$handRotZ( gunRotZ + DevHelper.get( 0 ).getRot().z );
		
		arm.handPos.set( this.handPos );
		arm.$handRotZ( gunRotZ + this.handRotZ );
		
//		final float armRot = gunRotZ * this.armRotFactor + DevHelper.get( 0 ).getRot().x;
		final float armRot = gunRotZ * this.armRotGunFactor + this.armRotZ;
		arm.armRotZ = MathHelper.clamp( armRot, 0F, 90F );
		
		this.updateArm( arm, animator );
	}
}
