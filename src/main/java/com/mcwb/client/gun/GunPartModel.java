package com.mcwb.client.gun;

import java.util.ArrayList;
import java.util.Collection;

import org.lwjgl.opengl.GL11;

import com.mcwb.client.item.IEquippedItemRenderer;
import com.mcwb.client.item.ItemModel;
import com.mcwb.client.module.IDeferredRenderer;
import com.mcwb.client.render.IAnimator;
import com.mcwb.common.gun.IGunPart;
import com.mcwb.common.item.IEquippedItem;
import com.mcwb.util.ArmTracker;
import com.mcwb.util.Mat4f;
import com.mcwb.util.Quat4f;
import com.mcwb.util.Vec3f;

import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public abstract class GunPartModel<
	C extends IGunPart< ? >,
	E extends IEquippedItem< ? extends C >,
	ER extends IEquippedItemRenderer< ? super E >,
	R extends IGunPartRenderer< ? super C, ? extends ER >
> extends ItemModel< C, E, ER, R >
{
	
	/**
	 * Render queue is introduced here to help with rendering the objects that is transparent
	 * FIXME: do not use same queue for first person hand and third-person view!
	 *     Because rendering scope glass texture could also trigger other third-person render and
	 *     this will break the queue state if they are the same
	 */
	protected static final ArrayList< IDeferredRenderer > HAND_QUEUE_0 = new ArrayList<>();
	protected static final ArrayList< IDeferredRenderer > HAND_QUEUE_1 = new ArrayList<>();
	
	protected static final ArrayList< IDeferredRenderer > QUEUE_0 = new ArrayList<>();
	protected static final ArrayList< IDeferredRenderer > QUEUE_1 = new ArrayList<>();
	
	private static final Vec3f MODIFY_POS = new Vec3f( 0F, 0F, 200F / 160F );
	
	protected Vec3f modifyPos = MODIFY_POS;
	
	/**
	 * This animation channel will be applied to each module as part of installed transform
	 */
	protected String moduleAnimationChannel = ""; // TODO: optimize performance
	
	protected abstract class GunPartRenderer implements IGunPartRenderer< C, ER >, IAnimator
	{
		protected final Mat4f mat = new Mat4f();
		protected IAnimator wrapped;
		
		@Override
		public void getPos( String channel, Vec3f dst )
		{
			switch( channel )
			{
			case CHANNEL_INSTALL:
				this.mat.get( dst );
				break;
				
			default: this.wrapped.getPos( channel, dst );
			}
		}
		
		@Override
		public void getRot( String channel, Quat4f dst )
		{
			switch( channel )
			{
			case CHANNEL_INSTALL:
				dst.set( this.mat );
				break;
				
			default: this.wrapped.getRot( channel, dst );
			}
		}
		
		@Override
		public void getChannel( String channel, Mat4f dst )
		{
			switch( channel )
			{
			case CHANNEL_INSTALL:
				dst.set( this.mat );
				break;
				
			default: this.wrapped.getChannel( channel, dst );
			}
		}
		
		@Override
		public float getFactor( String channel ) { return this.wrapped.getFactor( channel ); }
		
		@Override
		public void render( C contexted ) { GunPartModel.this.render( contexted ); }
		
		@Override
		public void prepareRender(
			C contexted, IAnimator animator,
			Collection< IDeferredRenderer > renderQueue0,
			Collection< IDeferredRenderer > renderQueue1
		) {
			this.wrapped = animator;
			animator.getChannel( CHANNEL_INSTALL, this.mat );
			animator.applyChannel( GunPartModel.this.moduleAnimationChannel, this.mat );
			
			renderQueue0.add( () -> {
				GL11.glPushMatrix();
				final Mat4f mat = Mat4f.locate();
				this.wrapped.getChannel( CHANNEL_ITEM, mat );
				mat.mul( this.mat ); // TODO: validate order
				glMultMatrix( mat );
				mat.release();
				
				final ResourceLocation texture = contexted.texture();
				contexted.modifyState().doRecommendedRender( texture, GunPartModel.this::render );
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
			mat.mul( this.mat ); // TODO: animator?
			mat.transformAsPoint( arm.handPos );
			mat.release();
			
			arm.updateArmOrientation();
		}
		
		protected class EquippedGunPartRenderer extends EquippedItemRenderer
		{
			@Override
			public void prepareRenderInHandSP( E equipped, EnumHand hand )
			{
				super.prepareRenderInHandSP( equipped, hand );
				
				// Blend modify transform
				final float alpha = this.animation.getFactor( CHANNEL_MODIFY );
				this.pos.interpolate( GunPartModel.this.modifyPos, alpha );
				
				final Quat4f quat = Quat4f.locate();
				this.animation.getRot( CHANNEL_MODIFY, quat );
				this.rot.interpolate( quat, alpha );
				quat.release();
				
				// Clear previous state
				HAND_QUEUE_0.forEach( IDeferredRenderer::release );
				HAND_QUEUE_0.clear();
				HAND_QUEUE_1.forEach( IDeferredRenderer::release );
				HAND_QUEUE_1.clear();
				
				// Prepare render queue
				equipped.item().prepareInHandRenderSP(
					equipped.animator(), // TODO: equipped#animator() should actually be #this
					HAND_QUEUE_0, HAND_QUEUE_1
				);
				
				// TODO: better comparator?
				HAND_QUEUE_1.sort( ( r0, r1 ) -> r0.priority() > r1.priority() ? -1 : 1 );
				HAND_QUEUE_1.forEach( IDeferredRenderer::prepare );
			}
			
			@Override
			protected void doRenderInHandSP( E equipped, EnumHand hand )
			{
				HAND_QUEUE_0.forEach( IDeferredRenderer::render );
				HAND_QUEUE_1.forEach( IDeferredRenderer::render );
			}
		}
	}
}
