package com.mcwb.client.gun;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import com.google.gson.annotations.SerializedName;
import com.mcwb.client.MCWBClient;
import com.mcwb.client.input.InputHandler;
import com.mcwb.client.player.PlayerPatchClient;
import com.mcwb.client.render.IRenderer;
import com.mcwb.common.MCWB;
import com.mcwb.common.MCWBResource;
import com.mcwb.common.gun.IGun;
import com.mcwb.common.load.BuildableLoader;
import com.mcwb.devtool.DevHelper;
import com.mcwb.util.ArmTracker;
import com.mcwb.util.Mat4f;
import com.mcwb.util.Util;
import com.mcwb.util.Vec3f;

import net.minecraft.block.material.Material;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.MovementInput;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class GunRenderer< T extends IGun > extends GripRenderer< T >
{
	public static final BuildableLoader< IRenderer >
		LOADER = new BuildableLoader<>(
			"gun", json -> MCWB.GSON.fromJson( json, GunRenderer.class )
		); // TODO: kind of weird as passing class works with ide but fails the compile
	
	protected static final Vec3f HOLD_POS = new Vec3f( -14F / 160F, -72F / 160F, 87.5F / 160F );
	protected static final Vec3f HOLD_ROT = new Vec3f( 0F, 0F, -5F );
	
	protected static final Vec3f MODIFY_POS = new Vec3f( 0F, -20F / 160F, 150F / 160F );
	
	// TODO: these vectors can be initialized with static variables as gson will create new instance on read
	protected Vec3f motionInertiaPos = new Vec3f( -0.2F, -0.2F, -0.2F );
	protected Vec3f motionInertiaRot = new Vec3f( -10F, 5F, 20F );
	
	protected Vec3f viewInertiaPos = new Vec3f( 0.001F, 0.001F, 0F );
	protected Vec3f viewInertiaRot = new Vec3f( 0.15F, 0.1F, -0.15F );
	
	protected Vec3f aimPos = new Vec3f( 0F, -64F / 160F, 81.5F / 160F );
	protected Vec3f crouchPos = new Vec3f( -28F / 160F, -64F / 160F, 87.5F / 160F );
	protected Vec3f sprintPos = new Vec3f( 5F / 160F, -96F / 160F, 81.5F / 160F );
	
	protected Vec3f aimRot = Vec3f.ORIGIN;
	protected Vec3f crouchRot = new Vec3f( 0F, 0F, -45F );
	protected Vec3f sprintRot = new Vec3f( 20F, 27.5F, -25F );
	
	protected Vec3f walkAmplPos = new Vec3f( 0.05F, 0.05F, 0F );
	protected Vec3f walkAmplRot = new Vec3f( 5F, 0F, 15F );
	protected Vec3f sprintAmplPos = new Vec3f( 0.1F * 1.5F, 0.16F * 1.75F, 0F );
	protected Vec3f sprintAmplRot = new Vec3f( 15F * 1.125F, -15F * 1.125F, 30F );
	
	protected Vec3f moveOffset = new Vec3f( 0F, -3.25F / 160F, -1F / 160F );
	protected Vec3f aimMoveOffset = new Vec3f( 0F, 0F, -0.5F / 160F );
	protected Vec3f crouchMoveOffset = new Vec3f( 0F, -1.5F / 160F, -0.5F / 160F );
	protected Vec3f sprintMoveOffset = new Vec3f( 0F, -4.25F / 160F, 1.3F / 160F );
	
	protected Vec3f shoulderOffset = new Vec3f( 0F, 0.0005F, 0.001F );
	protected Vec3f aimShoulderOffset = new Vec3f( 0F, 0F, 0.0005F );
	protected Vec3f crouchShoulderOffset = this.shoulderOffset;
	protected Vec3f sprintShoulderOffset = this.shoulderOffset;
	
	protected float dropCycle = Util.PI * 0.3F;
	protected float walkCycle = Util.PI * 0.6F;
	
	@SerializedName( value = "crouchWalkCycle", alternate = "sneakWalkCycle" )
	protected float crouchWalkCycle = Util.PI;
	
	public GunRenderer()
	{
		this.holdPos = HOLD_POS;
		this.holdRot = HOLD_ROT;
		this.modifyPos = MODIFY_POS;
	}
	
	@Override
	public void tickInHand( T contexted, EnumHand hand )
	{
		/// Prepare necessary variables ///
		final GunAnimatorState state = this.animator( hand );
		state.modifyOp = this.opModify();
		state.modifyPos = this.modifyPos;
		
		final Mat4f mat = state.m0;
		final EntityPlayerSP player = MCWBClient.MC.player;
		final PlayerPatchClient patch = PlayerPatchClient.instance;
		final boolean aiming = InputHandler.AIM_HOLD.down;
		final boolean crouching = player.isSneaking();
		final boolean sprinting = player.isSprinting();
		
		// Get motion of the player
		final Vec3f playerSpeed = patch.playerVelocity;
		final float dropSpeed = Math.min( 0F, playerSpeed.y );
		final float moveSpeed = player.onGround
			? MathHelper.sqrt( playerSpeed.x * playerSpeed.x + playerSpeed.z * playerSpeed.z ) : 0F;
		
		GunAnimatorState.dropDistanceCycle += dropSpeed * this.dropCycle;
		GunAnimatorState.walkDistanceCycle += moveSpeed
			* ( crouching ? this.crouchWalkCycle : this.walkCycle );
		
		// TODO: Check drop impact
		
		/// Camera acceleration impact ///
		// TODO: camera control
		
		/// Weapon acceleration impact ///
		{
			// Translate acceleration into walk direction
			final Vec3f acc = state.v0;
			patch.cameraController.getPlayerRot( acc );
			mat.setIdentity();
			mat.rotateX( -acc.x );
			mat.rotateY( acc.y );
			
			acc.set( patch.playerAcceleration );
			mat.apply( acc );
			
			// Apply gun body shift
			final Vec3f ip = this.motionInertiaPos;
			final Vec3f ir = this.motionInertiaRot;
			state.holdPos.velocity.translate( acc.x * ip.x, acc.y * ip.y, acc.z * ip.z );
			state.holdRot.velocity.translate( acc.y * ir.x, acc.x * ir.y, acc.x * ir.z );
		}
		
		/// Weapon walk/sprint bobbing ///
		{
			final Vec3f velo = state.v0;
			velo.set( patch.playerVelocity );
			mat.apply( velo );
			
			final float cos = moveSpeed
				* MathHelper.cos( GunAnimatorState.walkDistanceCycle + 0.5F * Util.PI );
			final float sin = Math.abs( cos ) - 0.5F * moveSpeed;
			final Vec3f ap = sprinting ? this.sprintAmplPos : this.walkAmplPos;
			final Vec3f ar = sprinting ? this.sprintAmplRot : this.walkAmplRot;
			state.holdPos.velocity.translate( cos * ap.x, sin * ap.y, cos * ap.z );
			state.holdRot.velocity.translate( sin * ar.x, cos * ar.y, cos * ar.z );
		}
		
		/// Smooth on view rotation ///
		{
			final float deltaPitch = player.rotationPitch - GunAnimatorState.prevPlayerPitch;
			final float deltaYaw = player.rotationYaw - GunAnimatorState.prevPlayerYaw;
			final Vec3f ip = this.viewInertiaPos;
			final Vec3f ir = this.viewInertiaRot;
			state.holdPos.velocity.translate( deltaYaw * ip.x, deltaPitch * ip.y, 0F );
			state.holdRot.velocity.translate( deltaPitch * ir.x, deltaYaw * ir.y, deltaYaw * ir.z );
			
			GunAnimatorState.prevPlayerPitch = player.rotationPitch;
			GunAnimatorState.prevPlayerYaw = player.rotationYaw;
		}
		
		/// Setup target orientation and update ///
		{
			final Vec3f pos = state.holdPos.tarPos;
			pos.set(
				aiming ? this.aimPos
					: sprinting ? this.sprintPos
						: crouching ? this.crouchPos : this.holdPos
			);
			
			final Vec3f mo = aiming ? this.aimMoveOffset
				: sprinting ? this.sprintMoveOffset
					: crouching ? this.crouchMoveOffset : this.moveOffset;
			final MovementInput input = player.movementInput;
			final boolean moving = input.forwardKeyDown
				|| input.backKeyDown || input.leftKeyDown || input.rightKeyDown;
			pos.translate( moving ? mo : Vec3f.ORIGIN );
			
			// TODO: maybe move to outer layer and avoid the delay introduce by motion tendency
			final Vec3f so = state.v0;
			patch.cameraController.getPlayerRot( so );
			final float pitch = so.x;
			so.set(
				aiming ? this.aimShoulderOffset
					: sprinting ? this.sprintShoulderOffset
						: crouching ? this.crouchShoulderOffset : this.shoulderOffset
			);
			so.scale( pitch );
			pos.translate( so );
			
			state.holdPos.update();
			
			// Rotation
			final Vec3f rot = state.holdRot.tarPos;
			rot.set(
				aiming ? this.aimRot
					: sprinting ? this.sprintRot
						: crouching ? this.crouchRot : this.holdRot
			);
			
			state.holdRot.update();
		}
	}
	
	@Override
	protected void doRenderInHand( T contexted, EnumHand hand )
	{
		// Re-setup projection matrix
		GL11.glMatrixMode( GL11.GL_PROJECTION );
		GL11.glLoadIdentity();
		Project.gluPerspective(
			this.getFovModifier(),
			( float ) MCWBClient.MC.displayWidth / MCWBClient.MC.displayHeight,
			0.05F, // TODO: maybe smaller this value to avoid seeing through the parts
			MCWBClient.SETTINGS.renderDistanceChunks * 16 * MathHelper.SQRT_2
		);
		GL11.glMatrixMode( GL11.GL_MODELVIEW );
		
		// For arm adjust
//		if( DevHelper.flag )
//		{
//			GL11.glTranslatef( 0F, 4F / 16f, 15f / 16f );
//			
//			final EntityPlayer player = MCWBClient.MC.player;
//			GL11.glRotatef( -player.rotationPitch, 1F, 0F, 0F );
//			GL11.glRotatef( player.rotationYaw, 0F, 1F, 0F );
//			
//			GL11.glTranslatef( 0F, 0F, -5F/16f );
//		}
		
//		final ResourceLocation texture = new MCWBResource( "textures/debug_box.png" );
//		final Vec3f p = DevHelper.get( 0 ).getPos();
//		GL11.glTranslatef( p.x, p.y, p.z );
//		
//		GL11.glEnable( GL11.GL_STENCIL_TEST );
//		
//		GL11.glStencilFunc( GL11.GL_NEVER, 1, 0xFF );
//		GL11.glStencilMask( 0xFF );
//		GL11.glStencilOp( GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP );
//		this.bindTexture( texture );
//		
//		final boolean flag = GL11.glIsEnabled( GL11.GL_STENCIL_TEST );
//		DevHelper.DEBUG_BOX.render();
//		GL11.glDisable( GL11.GL_STENCIL_TEST );
		
//		this.bindTexture( TEXTURE_BLUE );
//		GL11.glPushMatrix();
//		GL11.glTranslatef( 0F, 0F, 2F / 16F );
//		DevHelper.DEBUG_BOX.render();
//		GL11.glPopMatrix();
//		
//		this.bindTexture( TEXTURE_GREEN );
//		GL11.glPushMatrix();
//		GL11.glTranslatef( 0F, 0F, -2F / 16F );
//		final float scale = 0.1F;
//		GL11.glScalef( scale, scale, scale );
//		DevHelper.DEBUG_BOX.render();
//		GL11.glPopMatrix();
//		
//		GL11.glEnable( GL11.GL_STENCIL_TEST );
//		GL11.glClearStencil( 0 );
//		GL11.glClear( GL11.GL_STENCIL_BUFFER_BIT );
//		
		// Glass
//		GL11.glStencilOp( GL11.GL_KEEP, GL11.GL_REPLACE, GL11.GL_REPLACE );
//		GL11.glStencilFunc( GL11.GL_ALWAYS, 1, 0xFF );
//		GL11.glStencilMask( 0xFF );
//		
//		this.bindTexture( texture );
//		GL11.glEnable( GL11.GL_BLEND );
//		GL11.glColor4f( 1F, 1F, 1F, 0.5F );
//		DevHelper.DEBUG_BOX.render();
//		GL11.glDisable( GL11.GL_BLEND );
//		
//		// reticle
//		GL11.glStencilFunc( GL11.GL_NEVER, 1, 0xFF );
//		GL11.glStencilMask( 0x00 );
//		GL11.glStencilOp( GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP );
//		
//		GL11.glDisable( GL11.GL_DEPTH_TEST );
//		
//		final Vec3f pp = DevHelper.get( 1 ).getPos();
//		GL11.glTranslatef( pp.x, pp.y, pp.z );
//		this.bindTexture( TEXTURE_RED );
//		DevHelper.DEBUG_BOX.render();
//		
//		GL11.glEnable( GL11.GL_DEPTH_TEST );
//		GL11.glDisable( GL11.GL_STENCIL_TEST );
		
		
//		super.doRenderInHand( contexted, hand );
//		
//		// Render hand // TODO: hand animation
//		contexted.modifyState().doRenderArm( () -> {
//			final GunAnimatorState animator = this.animator( hand );
//			final ArmTracker leftArm = animator.leftArm;
//			final ArmTracker rightArm = animator.rightArm;
//			contexted.setupRenderArm( leftArm, rightArm, animator );
//			
//			this.renderArm( leftArm );
//			this.renderArm( rightArm );
//		} );
	}
	
	@Override
	protected GunAnimatorState animator( EnumHand hand ) { return GunAnimatorState.INSTANCE; }
	
	protected void renderArm( ArmTracker arm )
	{
		GL11.glPushMatrix(); {
		
		final Vec3f pos = arm.handPos;
		GL11.glTranslatef( pos.x, pos.y, pos.z );
		
		DevHelper.DEBUG_BOX.render();
		
		final Vec3f rot = arm.handRot;
		GL11.glRotatef( rot.y, 0F, 1F, 0F );
		GL11.glRotatef( rot.x, 1F, 0F, 0F );
		GL11.glRotatef( rot.z, 0F, 0F, 1F );
		
		this.bindTexture( TEXTURE_STEVE );
		
		GL11.glEnable( GL11.GL_BLEND );
		GL11.glColor4f( 1F, 1F, 1F, 0.5F );
		STEVE_ARM.render();
		GL11.glDisable( GL11.GL_BLEND );
		
		} GL11.glPopMatrix();
	}
	
	/**
	 * Copied from {@link EntityRenderer#getFOVModifier(float, boolean)}
	 */
	protected final float getFovModifier()
	{
		final float fov = MCWBClient.SETTINGS.fovSetting;
		return(
			ActiveRenderInfo.getBlockStateAtEntityViewpoint(
				MCWBClient.MC.world,
				MCWBClient.MC.getRenderViewEntity(),
				this.smoother()
			).getMaterial() == Material.WATER
			? 6F / 7F * fov
			: fov
		);
	}
	
	/* for test
	GL11.glTranslatef( 0F, 4F / 16f, 15f / 16f );
	
	final EntityPlayer player = MCWBClient.MC.player;
	GL11.glRotatef( -player.rotationPitch, 1F, 0F, 0F );
	GL11.glRotatef( player.rotationYaw, 0F, 1F, 0F );
	
	GL11.glTranslatef( 0F, 0F, -5F/16f );
	
	this.renderArm( DevHelper.INS.get( 0 ).getPos(), new Vec3f( 5F / 16F, -4F / 16F, 0F / 16F ) );
	this.renderArm( DevHelper.INS.get( 1 ).getPos(), new Vec3f( -3F / 16F, -4F / 16F, -2.5F / 16F ) );
	
	void renderArm( Vec3f handPos, Vec3f shoulderPos )
	{
		final ArmTracker tracker = new ArmTracker();
		tracker.shoulderPos.set( shoulderPos );
		tracker.handPos.set( handPos );
//		final Vec3f r = DevHelper.INS.cur().getRot();
//		tracker.handRot.z = r.z;
//		tracker.armRotZ = r.x;
		tracker.updateArmOrientation();
		
		GL11.glPushMatrix(); {
		
		final Vec3f pos = tracker.shoulderPos;
		GL11.glTranslatef( pos.x, pos.y, pos.z );
		DevHelper.DEBUG_BOX.render();
		
		} GL11.glPopMatrix();
		
		GL11.glPushMatrix(); {
		
		final Vec3f pos = tracker.elbowPos;
		GL11.glTranslatef( pos.x, pos.y, pos.z );
		DevHelper.DEBUG_BOX.render();
		
		} GL11.glPopMatrix();
		
		GL11.glPushMatrix(); {
		
		final Vec3f pos = tracker.handPos;
		GL11.glTranslatef( pos.x, pos.y, pos.z );
		DevHelper.DEBUG_BOX.render();
		
		} GL11.glPopMatrix();
		
		GL11.glPushMatrix(); {
		
		final Vec3f pos = tracker.handPos;
		GL11.glTranslatef( pos.x, pos.y, pos.z );
		
		final Vec3f rot = tracker.handRot;
		GL11.glRotatef( rot.y, 0F, 1F, 0F );
		GL11.glRotatef( rot.x, 1F, 0F, 0F );
		GL11.glRotatef( rot.z, 0F, 0F, 1F );
		GL11.glRotatef( -90F, 0F, 1F, 0F );
		this.bindTexture( new MCWBResource( "textures/steve.png" ) );
		GL11.glEnable( GL11.GL_BLEND );
		GL11.glColor4f( 1F, 1, 1, 0.5F );
		MCWBClient.STEVE_ARM.render();
		GL11.glDisable( GL11.GL_BLEND );
		
		} GL11.glPopMatrix();
	}
	/** for test */
}
