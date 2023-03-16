package com.mcwb.client.gun;

import com.mcwb.client.item.IEquippedItemRenderer;
import com.mcwb.client.render.IAnimator;
import com.mcwb.common.gun.IGunPart;
import com.mcwb.common.item.IEquippedItem;
import com.mcwb.common.load.BuildableLoader;
import com.mcwb.common.load.IContentProvider;
import com.mcwb.util.ArmTracker;
import com.mcwb.util.Mat4f;
import com.mcwb.util.Vec3f;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public abstract class GripModel<
	C extends IGunPart< ? >,
	E extends IEquippedItem< ? extends C >,
	ER extends IEquippedItemRenderer< ? super E >,
	R extends IGunPartRenderer< ? super C, ? extends ER >
> extends GunPartModel< C, E, ER, R >
{
	public static final BuildableLoader< ? >
		LOADER = new BuildableLoader<>( "grip", JsonGripModel.class );
	
	protected Vec3f handPos = Vec3f.ORIGIN;
	protected float handRotZ = 0F;
	protected float armRotZ = 0F;
	
	@Override
	public Object build( String path, IContentProvider provider )
	{
		super.build( path, provider );
		
		this.handPos.scale( this.scale );
		return this;
	}
	
	protected abstract class GripRenderer extends GunPartRenderer
	{
		@Override
		public void setupLeftArmToRender( IAnimator animator, ArmTracker leftArm ) {
			this.doSetupArmToRender( animator, leftArm );
		}
		
		@Override
		public void setupRightArmToRender( IAnimator animator, ArmTracker rightArm ) {
			this.doSetupArmToRender( animator, rightArm );
		}
		
		protected void doSetupArmToRender( IAnimator animator, ArmTracker arm )
		{
			final Mat4f mat = Mat4f.locate();
			animator.getChannel( CHANNEL_ITEM, mat ); // TODO: change to module, and default hand rot
			final float gunRotZ = mat.getEulerAngleZ();
			mat.release();
			
//			arm.handPos.set( DevHelper.get( 0 ).getPos() );
//			arm.$handRotZ( gunRotZ + DevHelper.get( 0 ).getRot().z );
//			arm.armRotZ = DevHelper.get( 0 ).getRot().x;
			
			arm.handPos.set( GripModel.this.handPos );
			arm.setHandRotZ( gunRotZ + GripModel.this.handRotZ );
			arm.armRotZ = GripModel.this.armRotZ;
			
			this.updateArm( arm, animator );
		}
	}
}
