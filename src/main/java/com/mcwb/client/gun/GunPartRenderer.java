package com.mcwb.client.gun;

import java.util.ArrayList;
import java.util.Collection;

import org.lwjgl.opengl.GL11;

import com.mcwb.client.item.IEquippedItemRenderer;
import com.mcwb.client.item.ItemRenderer;
import com.mcwb.client.module.IDeferredPriorityRenderer;
import com.mcwb.client.module.IDeferredRenderer;
import com.mcwb.client.render.IAnimator;
import com.mcwb.common.MCWB;
import com.mcwb.common.gun.IGunPart;
import com.mcwb.common.item.IEquippedItem;
import com.mcwb.common.load.BuildableLoader;
import com.mcwb.util.ArmTracker;
import com.mcwb.util.Mat4f;
import com.mcwb.util.Quat4f;
import com.mcwb.util.Vec3f;

import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class GunPartRenderer< C extends IGunPart< ? >, E extends IEquippedItem< ? extends C > >
	extends ItemRenderer< C, E > implements IGunPartRenderer< C, E >
{
	public static final BuildableLoader< ? > LOADER = new BuildableLoader<>( "gun_part",
		json -> MCWB.GSON.fromJson( json, GunPartRenderer.class )
	);
	
	/**
	 * Render queue is introduced here to help with rendering the objects with transparency
	 * FIXME: do not use same queue for first person hand and third-person view!
	 *     Because rendering scope glass texture could also trigger other third-person render and
	 *     this will break the queue state if they are the same
	 */
	protected static final ArrayList< IDeferredRenderer > HAND_QUEUE_0 = new ArrayList<>();
	protected static final ArrayList< IDeferredPriorityRenderer > HAND_QUEUE_1 = new ArrayList<>();

	protected static final ArrayList< IDeferredRenderer > QUEUE_0 = new ArrayList<>();
	protected static final ArrayList< IDeferredPriorityRenderer > QUEUE_1 = new ArrayList<>();
	
	private static final Vec3f MODIFY_POS = new Vec3f( 0F, 0F, 200F / 160F );
	
	protected Vec3f modifyPos = MODIFY_POS;
	
	@Override
	public IEquippedItemRenderer< E > onTakeOut( EnumHand hand ) {
		return this.new EquippedGunPartRenderer();
	}
	
	@Override
	public void prepareRender(
		C contexted,
		IAnimator animator,
		Collection< IDeferredRenderer > renderQueue0,
		Collection< IDeferredPriorityRenderer > renderQueue1
	) {
		renderQueue0.add( () -> {
			GL11.glPushMatrix();
			final Mat4f mat = Mat4f.locate();
			animator.getChannel( CHANNEL_ITEM, mat ); // TODO: for wrapped animator this takes time to get back to base
			animator.applyChannel( CHANNEL_INSTALL, mat );
			glMultMatrix( mat );
			mat.release();
			
			contexted.modifyState().doRecommendedRender( contexted.texture(), this::render );
			GL11.glPopMatrix();
		} );
	}
	
	@Override
	public void setupLeftArmToRender( ArmTracker leftArm, IAnimator animator )
	{
		leftArm.handPos.setZero();
		leftArm.armRotZ = 0F;
		leftArm.$handRotZ( 0F );
		this.updateArm( leftArm, animator );
	}
	
	@Override
	public void setupRightArmToRender( ArmTracker rightArm, IAnimator animator )
	{
		rightArm.handPos.setZero();
		rightArm.armRotZ = 0F;
		rightArm.$handRotZ( 0F );
		this.updateArm( rightArm, animator );
	}
	
	protected void updateArm( ArmTracker arm, IAnimator animator )
	{
		final Mat4f mat = Mat4f.locate();
		animator.getChannel( CHANNEL_ITEM, mat );
		animator.applyChannel( CHANNEL_INSTALL, mat );
		mat.transformAsPoint( arm.handPos );
		mat.release();
		
		arm.updateArmOrientation();
	}
	
	protected class EquippedGunPartRenderer extends EquippedItemRenderer
	{
		@Override
		public void tickInHand( E equipped, EnumHand hand ) { }
		
		@Override
		public void prepareRenderInHandSP( E equipped, EnumHand hand )
		{
			super.prepareRenderInHandSP( equipped, hand );
			
			// Blend modify transform
			final float alpha = this.animation.getFactor( CHANNEL_MODIFY );
			this.pos.interpolate( GunPartRenderer.this.modifyPos, alpha );
			
			final Quat4f quat = Quat4f.locate();
			this.animation.getRot( CHANNEL_MODIFY, quat );
			this.rot.interpolate( quat, alpha );
			quat.release();
			
			// Clear previous state
			HAND_QUEUE_0.forEach( IDeferredRenderer::release );
			HAND_QUEUE_0.clear();
			HAND_QUEUE_1.forEach( IDeferredPriorityRenderer::release );
			HAND_QUEUE_1.clear();
			
			// Prepare render queue
			equipped.item().prepareInHandRenderSP(
				equipped.animator(), // TODO: equipped#animator() should actually be #this
				HAND_QUEUE_0, HAND_QUEUE_1
			);
			
			// TODO: better comparator?
			HAND_QUEUE_1.sort( ( r0, r1 ) -> r0.priority() > r1.priority() ? -1 : 1 );
			HAND_QUEUE_1.forEach( IDeferredPriorityRenderer::prepare );
		}
		
		@Override
		protected void doRenderInHandSP( E equipped, EnumHand hand )
		{
			HAND_QUEUE_0.forEach( IDeferredRenderer::render );
			HAND_QUEUE_1.forEach( IDeferredPriorityRenderer::render );
		}
	}
}
