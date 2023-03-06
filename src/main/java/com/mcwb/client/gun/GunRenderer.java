package com.mcwb.client.gun;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import com.google.gson.annotations.SerializedName;
import com.mcwb.client.MCWBClient;
import com.mcwb.client.input.InputHandler;
import com.mcwb.client.player.PlayerPatchClient;
import com.mcwb.client.render.IAnimator;
import com.mcwb.client.render.IRenderer;
import com.mcwb.common.MCWB;
import com.mcwb.common.gun.IGun;
import com.mcwb.common.load.BuildableLoader;
import com.mcwb.common.load.IContentProvider;
import com.mcwb.util.ArmTracker;
import com.mcwb.util.Constants;
import com.mcwb.util.Mat4f;
import com.mcwb.util.Vec3f;

import net.minecraft.block.material.Material;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class GunRenderer< T extends IGun< ? > > extends GunPartRenderer< T >
{
	public static final BuildableLoader< IRenderer > LOADER =
		new BuildableLoader<>( "gun", json -> MCWB.GSON.fromJson( json, GunRenderer.class ) );
	
	protected static final Vec3f HOLD_POS = new Vec3f( -14F / 160F, -72F / 160F, 87.5F / 160F );
	protected static final Vec3f HOLD_ROT = new Vec3f( 0F, 0F, -5F );
	
	protected static final Vec3f MODIFY_POS = new Vec3f( 0F, -20F / 160F, 150F / 160F );
	
	protected final Vec3f rightHandPos = Vec3f.ORIGIN;
	protected float rightHandRotZ = 0F;
	protected float rightArmRotZ = 0F;
	
	protected final Vec3f leftHandPos = Vec3f.ORIGIN;
	protected float leftHandRotZ = 0F;
	protected float leftArmRotZ = 0F;
	
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
	
	protected float dropCycle = Constants.PI * 0.3F;
	protected float walkCycle = Constants.PI * 0.6F;
	
	@SerializedName( value = "crouchWalkCycle", alternate = "sneakWalkCycle" )
	protected float crouchWalkCycle = Constants.PI;
	
	public GunRenderer()
	{
		this.holdPos = HOLD_POS;
		this.holdRot = HOLD_ROT;
		this.modifyPos = MODIFY_POS;
	}
	
	@Override
	public IRenderer build( String path, IContentProvider provider )
	{
		super.build( path, provider );
		
		this.leftHandPos.scale( this.scale );
		this.rightHandPos.scale( this.scale );
		return this;
	}
	
	@Override
	public void tickInHand( T contexted, EnumHand hand )
	{
		/// Prepare necessary variables ///
		final GunAnimatorState state = this.animator( hand );
		state.modifyOp = contexted.opModify();
		state.modifyPos = this.modifyPos;
		
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
		
		final Vec3f vec = Vec3f.locate();
		final Mat4f mat = Mat4f.locate();
		
		/// Weapon acceleration impact ///
		{
			// Translate acceleration into walk direction
			final Vec3f acc = vec;
			patch.cameraController.getPlayerRot( acc );
			mat.setIdentity();
			mat.rotateX( -acc.x );
			mat.rotateY( acc.y );
			
			acc.set( patch.playerAcceleration );
			mat.transformAsPoint( acc );
			
			// Apply gun body shift
			final Vec3f ip = this.motionInertiaPos;
			final Vec3f ir = this.motionInertiaRot;
			state.holdPos.velocity.add( acc.x * ip.x, acc.y * ip.y, acc.z * ip.z );
			state.holdRot.velocity.add( acc.y * ir.x, acc.x * ir.y, acc.x * ir.z );
		}
		
		/// Weapon walk/sprint bobbing ///
		{
			final Vec3f velo = vec;
			velo.set( patch.playerVelocity );
			mat.transformAsPoint( velo );
			
			final float cos = moveSpeed
				* MathHelper.cos( GunAnimatorState.walkDistanceCycle + 0.5F * Constants.PI );
			final float sin = Math.abs( cos ) - 0.5F * moveSpeed;
			final Vec3f ap = sprinting ? this.sprintAmplPos : this.walkAmplPos;
			final Vec3f ar = sprinting ? this.sprintAmplRot : this.walkAmplRot;
			state.holdPos.velocity.add( cos * ap.x, sin * ap.y, cos * ap.z );
			state.holdRot.velocity.add( sin * ar.x, cos * ar.y, cos * ar.z );
		}
		
		/// Smooth on view rotation ///
		{
			final float deltaPitch = player.rotationPitch - GunAnimatorState.prevPlayerPitch;
			final float deltaYaw = player.rotationYaw - GunAnimatorState.prevPlayerYaw;
			final Vec3f ip = this.viewInertiaPos;
			final Vec3f ir = this.viewInertiaRot;
			state.holdPos.velocity.add( deltaYaw * ip.x, deltaPitch * ip.y, 0F );
			state.holdRot.velocity.add( deltaPitch * ir.x, deltaYaw * ir.y, deltaYaw * ir.z );
			
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
			pos.add( moving ? mo : Vec3f.ORIGIN );
			
			// TODO: maybe move to outer layer and avoid the delay introduce by motion tendency
			final Vec3f so = vec;
			patch.cameraController.getPlayerRot( so );
			final float pitch = so.x;
			so.set(
				aiming ? this.aimShoulderOffset
					: sprinting ? this.sprintShoulderOffset
						: crouching ? this.crouchShoulderOffset : this.shoulderOffset
			);
			so.scale( pitch );
			pos.add( so );
			
			state.holdPos.update(
				this.holdPosForceMult,
				this.holdPosMaxForce,
				this.holdPosDampingFactor
			);
			
			// Rotation
			final Vec3f rot = state.holdRot.tarPos;
			rot.set(
				aiming ? this.aimRot
					: sprinting ? this.sprintRot
						: crouching ? this.crouchRot : this.holdRot
			);
			
			state.holdRot.update(
				this.holdRotForceMult,
				this.holdRotMaxForce,
				this.holdRotDampingFactor
			);
		}
		
		mat.release();
		vec.release();
	}
	
	@Override
	public void setupLeftArmToRender( ArmTracker leftArm, IAnimator animator )
	{
		this.doSetupArmToRender(
			leftArm, animator,
			this.leftHandPos, this.leftHandRotZ, this.leftArmRotZ
		);
	}
	
	@Override
	public void setupRightArmToRender( ArmTracker rightArm, IAnimator animator )
	{
		this.doSetupArmToRender(
			rightArm, animator,
			this.rightHandPos, this.rightHandRotZ, this.rightArmRotZ
		);
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
		
		/* For arm adjust */
//		if( Dev.flag )
//		{
//			GL11.glTranslatef( 0F, 4F / 16f, 15f / 16f );
//			
//			final EntityPlayer player = MCWBClient.MC.player;
//			GL11.glRotatef( -player.rotationPitch, 1F, 0F, 0F );
//			GL11.glRotatef( player.rotationYaw, 0F, 1F, 0F );
//			
//			GL11.glTranslatef( 0F, 0F, -5F/16f );
//		}
		
		// Render hand // TODO: hand animation
		contexted.modifyState().doRenderArm( () -> {
			final GunAnimatorState animator = this.animator( hand );
			final ArmTracker leftArm = animator.leftArm;
			final ArmTracker rightArm = animator.rightArm;
			contexted.setupRenderArm( leftArm, rightArm, animator );
			
			this.renderArm( leftArm );
			this.renderArm( rightArm );
		} );
		
		super.doRenderInHand( contexted, hand );
	}
	
	protected void doSetupArmToRender(
		ArmTracker arm,
		IAnimator animator,
		Vec3f handPos,
		float handRotZ,
		float armRotZ
	) {
		final Mat4f mat = Mat4f.locate();
		animator.getChannel( CHANNEL_ITEM, this.smoother(), mat );
		final float gunRotZ = mat.getEulerAngleZ();
		mat.release();
		
//		arm.handPos.set( DevHelper.get( 0 ).getPos() );
//		arm.$handRotZ( gunRotZ + DevHelper.get( 0 ).getRot().z );
//		arm.armRotZ = DevHelper.get( 0 ).getRot().x;
		
		arm.handPos.set( handPos );
		arm.$handRotZ( gunRotZ + handRotZ );
		arm.armRotZ = armRotZ;
		
		this.updateArm( arm, animator );
	}
	
	protected void renderArm( ArmTracker arm )
	{
		GL11.glPushMatrix(); {
		
		glTranslatef( arm.handPos );
//		DevHelper.DEBUG_BOX.render();
		
		glEulerRotateYXZ( arm.handRot );
		
		this.bindTexture( TEXTURE_ALEX );
		
		GL11.glEnable( GL11.GL_BLEND );
//		GL11.glColor4f( 1F, 1F, 1F, 0.5F );
		ALEX_ARM.render();
		GL11.glDisable( GL11.GL_BLEND );
		
		} GL11.glPopMatrix();
	}
	
	@Override
	protected GunAnimatorState animator( EnumHand hand ) { return GunAnimatorState.INSTANCE; }
	
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
	
	/* for dynamic system test
	GL11.glPushMatrix();
	{
		Vec3f vec = new Vec3f();
		oldDynamic.get( vec, this.smoother() );
		glEulerRotateYXZ( vec );
		Dev.DEBUG_BOX.accept( true );
	}
	GL11.glPopMatrix();
	
	GL11.glPushMatrix();
	{
		Quat4f quat = new Quat4f();
		newDynamic.get( quat, this.smoother() );
		Mat4f mat = new Mat4f();
		mat.set( quat );
		glMultMatrix( mat );
		
		glScalef( 1.5F );
		GL11.glEnable( GL11.GL_BLEND );
		GL11.glColor4f( 1, 1, 1, 0.5f );
		Dev.DEBUG_BOX.accept( true );
		GL11.glDisable( GL11.GL_BLEND );
	}
	GL11.glPopMatrix();
	 */
	
	/* for arm test
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
