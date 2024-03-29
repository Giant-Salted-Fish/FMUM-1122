package com.oc.client.renderer;

import com.fmum.client.gun.IEquippedGunPartRenderer;
import com.fmum.client.gun.IGunPartRenderer;
import com.fmum.client.gun.JsonGripModel;
import com.fmum.client.render.IAnimator;
import com.fmum.common.gun.IGunPart;
import com.fmum.common.item.IEquippedItem;
import com.fmum.common.load.IContentProvider;
import com.fmum.util.ArmTracker;
import com.fmum.util.Mat4f;
import com.fmum.util.Vec3f;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class TDGripModel extends JsonGripModel
{
	public TDGripModel( String path, IContentProvider provider )
	{
		this.meshPath = "models/grip/td_grip.obj";
		this.tbObjAdapt = true;
		this.scale = 1F / 160F;
		this.handPos = new Vec3f( 0F, 20F, 0F );
		
		this.build( path, provider );
	}
	
	@Override
	public IGunPartRenderer<
		? super IGunPart< ? >,
		? extends IEquippedGunPartRenderer< ? super IEquippedItem< ? extends IGunPart< ? > > >
	> newRenderer()
	{
		return new GripRenderer()
		{
			@Override
			protected void doSetupArmToRender( IAnimator animator, ArmTracker arm )
			{
				final Mat4f mat = Mat4f.locate();
				animator.getChannel( CHANNEL_ITEM, mat );
				final float gunRotZ = mat.getEulerAngleZ();
				mat.release();
				
				final float installRotZ = this.mat.getEulerAngleZ() - gunRotZ;
//				leftArm.handPos.set( DevHelper.get( 0 ).getPos() );
//				leftArm.$handRotZ( gunRotZ + DevHelper.get( 0 ).getRot().z );
				
				arm.handPos.set( TDGripModel.this.handPos );
				arm.setHandRotZ( gunRotZ + ( installRotZ + 180F ) + TDGripModel.this.handRotZ );
				
				final boolean isTilt = installRotZ > -150F && installRotZ < -80F;
				final float tiltOffset = isTilt ? 35F + gunRotZ * 0.75F : 0F;
				final float armRot = TDGripModel.this.armRotZ + tiltOffset;
				arm.armRotZ = MathHelper.clamp( armRot, 0F, 90F );
				
				this.updateArm( arm, animator );
			}
			
			@Override
			public IEquippedGunPartRenderer<
				? super IEquippedItem< ? extends IGunPart< ? > >
			> onTakeOut( EnumHand hand ) { return new EquippedGunPartRenderer(); }
		};
	}
}
