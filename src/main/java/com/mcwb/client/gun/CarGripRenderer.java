package com.mcwb.client.gun;

import com.mcwb.client.render.IAnimator;
import com.mcwb.client.render.IRenderer;
import com.mcwb.common.MCWB;
import com.mcwb.common.gun.IGunPart;
import com.mcwb.common.load.BuildableLoader;
import com.mcwb.util.ArmTracker;
import com.mcwb.util.Mat4f;

import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class CarGripRenderer< T extends IGunPart > extends GripRenderer< T >
{
	public static final BuildableLoader< IRenderer >
		LOADER = new BuildableLoader<>(
			"car_grip", json -> MCWB.GSON.fromJson( json, CarGripRenderer.class )
		);
	
	protected float armRotGunFactor = 1F;
	
	@Override
	protected void doSetupArmToRender( ArmTracker arm, IAnimator animator )
	{
		final Mat4f mat = Mat4f.locate();
		animator.getChannel( CHANNEL_ITEM, this.smoother(), mat );
		final float gunRotZ = mat.getEulerAngleZ();
		mat.release();
		
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
