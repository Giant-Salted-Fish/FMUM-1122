package com.fmum.client.gun;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

import org.lwjgl.opengl.GL11;

import com.fmum.client.IAutowireBindTexture;
import com.fmum.client.FMUMClient;
import com.fmum.client.item.ItemModel;
import com.fmum.client.module.IDeferredRenderer;
import com.fmum.client.render.IAnimator;
import com.fmum.common.gun.IGunPart;
import com.fmum.common.item.IEquippedItem;
import com.fmum.common.load.IContentProvider;
import com.fmum.util.ArmTracker;
import com.fmum.util.IAnimation;
import com.fmum.util.Mat4f;
import com.fmum.util.Mesh;
import com.fmum.util.Quat4f;
import com.fmum.util.Vec3f;
import com.google.gson.annotations.SerializedName;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public abstract class GunPartModel<
	C extends IGunPart< ? >,
	E extends IEquippedItem< ? extends C >,
	ER extends IEquippedGunPartRenderer< ? super E >,
	R extends IGunPartRenderer< ? super C, ? extends ER >
> extends ItemModel< C, E, R >
{
	/**
	 * Render queue that helps to render transparent objects.
	 */
	// FIXME: do not use same queue for first person hand and third-person view!
	//     Because rendering scope glass texture could also trigger other third-person render and \
	//     this will break the queue state if they are the same.
	protected static final ArrayList< IDeferredRenderer > HAND_QUEUE_0 = new ArrayList<>();
	protected static final ArrayList< IDeferredRenderer > HAND_QUEUE_1 = new ArrayList<>();
	
	protected static final ArrayList< IDeferredRenderer > QUEUE_0 = new ArrayList<>();
	protected static final ArrayList< IDeferredRenderer > QUEUE_1 = new ArrayList<>();
	
	private static final Vec3f MODIFY_POS = new Vec3f( 0F, 0F, 200F / 160F );
	
	protected Vec3f modifyPos = MODIFY_POS;
	
	protected AnimatedMesh[] animatedMeshes = { };
	
	protected String moduleAnimationChannel = "";
	
	@Override
	public Object build( String path, IContentProvider provider )
	{
		super.build( path, provider );
		
		if ( this.moduleAnimationChannel.length() == 0 )
		{
			final int idx = this.meshPath.lastIndexOf( '/' ) + 1;
			this.moduleAnimationChannel = this.meshPath.substring( idx );
		}
		
		for ( AnimatedMesh aniMesh : this.animatedMeshes ) {
			aniMesh.origin.scale( this.scale );
		}
		return this;
	}
	
	@Override
	protected void onMeshLoad( IContentProvider provider )
	{
		super.onMeshLoad( provider );
		
		for ( AnimatedMesh aniMesh : this.animatedMeshes ) {
			aniMesh.mesh = this.loadMesh( aniMesh.meshPath, provider );
		}
	}
	
	protected void renderAnimatedMesh( IAnimator animator )
	{
		for ( AnimatedMesh aniMesh : this.animatedMeshes )
		{
			GL11.glPushMatrix();
			
			glTranslatef( aniMesh.origin );
			
			final Mat4f mat = Mat4f.locate();
			animator.getChannel( aniMesh.animationChannel, mat );
			glMulMatrix( mat );
			mat.release();
			
			aniMesh.mesh.render();
			
			GL11.glPopMatrix();
		}
	}
	
	protected abstract class GunPartRenderer
		implements IGunPartRenderer< C, ER >, IAutowireBindTexture
	{
		protected final Mat4f mat = new Mat4f();
		
		@Override
		public void getTransform( Mat4f dst ) { dst.set( this.mat ); }
		
		@Override
		public void prepareRender(
			C contexted, IAnimator animator,
			Collection< IDeferredRenderer > renderQueue0,
			Collection< IDeferredRenderer > renderQueue1
		) {
			contexted.base().getRenderTransform( contexted, animator, this.mat );
			animator.applyChannel( GunPartModel.this.moduleAnimationChannel, this.mat );
			
			// TODO: we can buffer animator so no instance will be created for this closure
			renderQueue0.add( () -> {
				GL11.glPushMatrix();
				glMulMatrix( this.mat );
				
				final ResourceLocation texture = contexted.texture();
				contexted.modifyState().doRecommendedRender( texture, () -> {
					GunPartModel.this.renderAnimatedMesh( animator );
					GunPartModel.this.render();
				} );
				GL11.glPopMatrix();
			} );
		}
		
		@Override
		public void setupLeftArmToRender( IAnimator animator, ArmTracker leftArm )
		{
			leftArm.handPos.setZero();
			leftArm.armRotZ = 0F;
			leftArm.setHandRotZ( 0F );
			this.updateArm( leftArm, animator );
		}
		
		@Override
		public void setupRightArmToRender( IAnimator animator, ArmTracker rightArm )
		{
			rightArm.handPos.setZero();
			rightArm.armRotZ = 0F;
			rightArm.setHandRotZ( 0F );
			this.updateArm( rightArm, animator );
		}
		
		@Override
		public void render( C contexted, IAnimator animator )
		{
			this.bindTexture( contexted.texture() );
			GunPartModel.this.render();
		}
		
		// TODO: caller of this method may also have get the same item channel
		protected void updateArm( ArmTracker arm, IAnimator animator ) {
			this.mat.transformAsPoint( arm.handPos );
		}
		
		protected class EquippedGunPartRenderer extends EquippedItemRenderer
			implements IEquippedGunPartRenderer< E >
		{
			// Not protected for the visibility problem of TDGripModel.
			public EquippedGunPartRenderer() { }
			
			@Override
			public void useModifyAnimation( Supplier< Float > refPlayerRotYaw ) {
				this.useAnimation( GunPartModel.this.new ModifyAnimator( this, refPlayerRotYaw ) );
			}
			
			@Override
			public void prepareRenderInHandSP( E equipped, EnumHand hand )
			{
				// Clear previous state.
				HAND_QUEUE_0.clear();
				HAND_QUEUE_1.clear();
				
				// Prepare render queue.
				// TODO: equipped#animator() should actually be #this
				final IAnimator animator = equipped.animator();
				equipped.item().prepareRenderInHandSP( animator, HAND_QUEUE_0, HAND_QUEUE_1 );
				
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
	
	protected class ModifyAnimator implements IAnimation
	{
		protected final IAnimator animator;
		protected final Supplier< Float > refPlayerRotYaw;
		
		protected final Vec3f pos = new Vec3f();
		protected final Quat4f rot = new Quat4f();
		
		protected ModifyAnimator( IAnimator animator, Supplier< Float > refPlayerRotYaw )
		{
			this.animator = animator;
			this.refPlayerRotYaw = refPlayerRotYaw;
		}
		
		@Override
		public void update( float progress )
		{
			final Mat4f mat = Mat4f.locate();
			this.animator.getChannel( CHANNEL_ITEM, mat );
			mat.invert();
			
			this.pos.set( GunPartModel.this.modifyPos );
			this.pos.scale( progress );
			mat.translate( this.pos );
			
			final EntityPlayer player = FMUMClient.MC.player;
			final float refPlayerRotYaw = this.refPlayerRotYaw.get();
			final float modifyYawBase = ( refPlayerRotYaw % 360F + 360F ) % 360F - 180F; // TODO: maybe do this when capture ref player yaw
			final float modifyYawDelta = refPlayerRotYaw - player.rotationYaw;
			final float modifyYaw = modifyYawBase - modifyYawDelta;
			
			mat.get( this.pos );
			this.rot.set( mat );
			
			this.pos.scale( progress );
			this.rot.scaleAngle( progress );
			
			Quat4f quat = Quat4f.locate();
			
			mat.setIdentity();
			mat.rotateX( -player.rotationPitch * progress );
			quat.set( mat );
			this.rot.mul( quat );
			
			mat.setIdentity();
			mat.rotateY( modifyYaw * progress );
			quat.set( mat );
			this.rot.mul( quat );
			
			quat.release();
			mat.release();
		}
		
		@Override
		public void getPos( String channel, Vec3f dst )
		{
			final boolean isThisChannel = channel.equals( GunPartModel.this.animationChannel );
			if ( isThisChannel ) { dst.set( this.pos ); }
			else { dst.setZero(); }
		}
		
		@Override
		public void getRot( String channel, Quat4f dst )
		{
			final boolean isThisChannel = channel.equals( GunPartModel.this.animationChannel );
			if ( isThisChannel ) { dst.set( this.rot ); }
			else { dst.clearRot(); }
		}
		
		@Override
		public float getFactor( String channel ) { return 1F; }
	}
	
	protected static class AnimatedMesh
	{
		@SerializedName( value = "mesh" )
		protected String meshPath;
		protected transient Mesh mesh;
		
		protected Vec3f origin = Vec3f.ORIGIN;
		protected String animationChannel = "";
	}
}
