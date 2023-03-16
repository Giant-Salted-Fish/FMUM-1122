package com.mcwb.client.gun;

import com.mcwb.client.item.IEquippedItemRenderer;
import com.mcwb.client.render.IAnimator;
import com.mcwb.common.gun.IGunPart;
import com.mcwb.common.item.IEquippedItem;
import com.mcwb.common.load.BuildableLoader;
import com.mcwb.util.ArmTracker;
import com.mcwb.util.Mat4f;

import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public abstract class CarGripModel<
	C extends IGunPart< ? >,
	E extends IEquippedItem< ? extends C >,
	ER extends IEquippedItemRenderer< ? super E >,
	R extends IGunPartRenderer< ? super C, ? extends ER >
> extends GripModel< C, E, ER, R >
{
	public static final BuildableLoader< ? >
		LOADER = new BuildableLoader<>( "car_grip", JsonCarGripModel.class );
	
	protected float armRotGunFactor = 1F;
	
	protected abstract class CarGripRenderer extends GripRenderer
	{
		@Override
		protected void doSetupArmToRender( IAnimator animator, ArmTracker arm )
		{
			final Mat4f mat = Mat4f.locate();
			animator.getChannel( CHANNEL_ITEM, mat );
			final float gunRotZ = mat.getEulerAngleZ();
			mat.release();
			
//			leftArm.handPos.set( Dev.get( 0 ).getPos() );
//			leftArm.$handRotZ( gunRotZ + Dev.get( 0 ).getRot().z );
			
			final CarGripModel< ?, ?, ?, ? > $this = CarGripModel.this;
			arm.handPos.set( $this.handPos );
			arm.setHandRotZ( gunRotZ + $this.handRotZ );
			
//			final float armRot = gunRotZ * this.armRotFactor + Dev.get( 0 ).getRot().x;
			final float armRot = gunRotZ * $this.armRotGunFactor + $this.armRotZ;
			arm.armRotZ = MathHelper.clamp( armRot, 0F, 90F );
			
			this.updateArm( arm, animator );
		}
	}
}
