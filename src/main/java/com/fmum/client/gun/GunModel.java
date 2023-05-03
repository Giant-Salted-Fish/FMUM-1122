package com.fmum.client.gun;

import java.util.function.Supplier;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import com.fmum.client.FMUMClient;
import com.fmum.client.input.Key;
import com.fmum.client.player.PlayerPatchClient;
import com.fmum.client.render.IAnimator;
import com.fmum.common.gun.IEquippedGun;
import com.fmum.common.gun.IGun;
import com.fmum.common.load.IContentProvider;
import com.fmum.util.ArmTracker;
import com.fmum.util.DynamicPos;
import com.fmum.util.Mat4f;
import com.fmum.util.Quat4f;
import com.fmum.util.Util;
import com.fmum.util.Vec3f;
import com.google.gson.annotations.SerializedName;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public abstract class GunModel<
	C extends IGun< ? >,
	E extends IEquippedGun< ? extends C >,
	ER extends IEquippedGunPartRenderer< ? super E >,
	R extends IGunRenderer< ? super C, ? extends ER >
> extends GunPartModel< C, E, ER, R >
{
	private static final Vec3f HOLD_POS = new Vec3f( -14F / 160F, -72F / 160F, 87.5F / 160F );
	private static final Vec3f HOLD_ROT = new Vec3f( 0F, 0F, -5F );
	
	private static final Vec3f MODIFY_POS = new Vec3f( 0F, -20F / 160F, 150F / 160F );
	
	// TODO: how to handle these static fields
	protected static float walkDistanceCycle = 0F;
	
	protected static float prevPlayerPitch = 0F;
	protected static float prevPlayerYaw = 0F;
	
	protected float holdPosForceMult = 0.25F;
	protected float holdPosMaxForce = 0.125F;
	protected float holdPosDampingFactor = 0.4F;
	
	protected float holdRotForceMult = 1F;
	protected float holdRotMaxForce = 4.25F;
	protected float holdRotDampingFactor = 0.4F;
	
	protected final Vec3f rightHandPos = Vec3f.ORIGIN;
	protected float rightHandRotZ = 0F;
	protected float rightArmRotZ = 0F;
	protected String leftArmAnimationChannel = "left_arm";
	
	protected final Vec3f leftHandPos = Vec3f.ORIGIN;
	protected float leftHandRotZ = 0F;
	protected float leftArmRotZ = 0F;
	protected String rightArmAnimationChannel = "right_arm";
	
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
	
	protected float walkCycle = Util.PI * 0.6F;
	
	@SerializedName( value = "crouchWalkCycle", alternate = "sneakWalkCycle" )
	protected float crouchWalkCycle = Util.PI;
	
	public GunModel()
	{
		this.holdPos = HOLD_POS;
		this.holdRot = HOLD_ROT;
		this.modifyPos = MODIFY_POS;
	}
	
	@Override
	public Object build( String path, IContentProvider provider )
	{
		super.build( path, provider );
		
		this.leftHandPos.scale( this.scale );
		this.rightHandPos.scale( this.scale );
		return this;
	}
	
	/**
	 * Copied from {@link EntityRenderer#getFOVModifier(float, boolean)}.
	 */
	protected final float getFovModifier( float smoother )
	{
		final World world = FMUMClient.MC.world;
		final Entity cameraEntity = FMUMClient.MC.getRenderViewEntity();
		final IBlockState blockAtCamera = ActiveRenderInfo
			.getBlockStateAtEntityViewpoint( world, cameraEntity, smoother );
		boolean isInWater = blockAtCamera.getMaterial() == Material.WATER;
		
		final float fov = FMUMClient.SETTINGS.fovSetting;
		return isInWater ? 6F / 7F * fov : fov;
	}
	
	protected abstract class GunRenderer extends GunPartRenderer implements IGunRenderer< C, ER >
	{
		@Override
		public void setupLeftArmToRender( IAnimator animator, ArmTracker leftArm )
		{
			final GunModel< ?, ?, ?, ? > $this = GunModel.this;
			this.doSetupArmToRender(
				leftArm, animator,
				$this.leftHandPos, $this.leftHandRotZ, $this.leftArmRotZ
			);
		}
		
		@Override
		public void setupRightArmToRender( IAnimator animator, ArmTracker rightArm )
		{
			final GunModel< ?, ?, ?, ? > $this = GunModel.this;
			this.doSetupArmToRender(
				rightArm, animator,
				$this.rightHandPos, $this.rightHandRotZ, $this.rightArmRotZ
			);
		}
		
		protected void doSetupArmToRender(
			ArmTracker arm,
			IAnimator animator,
			Vec3f handPos,
			float handRotZ,
			float armRotZ
		) {
			final Mat4f mat = Mat4f.locate();
			animator.getChannel( CHANNEL_ITEM, mat );
			final float gunRotZ = mat.getEulerAngleZ();
			mat.release();
			
//			arm.handPos.set( DevHelper.get( 0 ).getPos() );
//			arm.$handRotZ( gunRotZ + DevHelper.get( 0 ).getRot().z );
//			arm.armRotZ = DevHelper.get( 0 ).getRot().x;
			
			arm.handPos.set( handPos );
			arm.setHandRotZ( gunRotZ + handRotZ );
			arm.armRotZ = armRotZ;
			
			this.updateArm( arm, animator ); // TODO: refactor this: it will actually get ITEM channel again
		}
		
		protected class EquippedGunRenderer extends EquippedGunPartRenderer
		{
			protected final DynamicPos holdPos = new DynamicPos();
			protected final DynamicPos holdRot = new DynamicPos();
			
			// TODO: this may also be static
			protected final ArmTracker leftArm = new ArmTracker();
			protected final ArmTracker rightArm = new ArmTracker();
			
			protected EquippedGunRenderer()
			{
				this.leftArm.shoulderPos.set( 5F / 16F, -4F / 16F, 0F );
				this.rightArm.shoulderPos.set( -3F / 16F, -4F / 16F, -2.5F / 16F );
			}
			
			@Override
			public void useModifyAnimation( Supplier< Float > refPlayerRotYaw )
			{
				this.useAnimation( new ModifyAnimator( this, refPlayerRotYaw ) {
					@Override
					public void getPos( String channel, Vec3f dst )
					{
						if (
							channel.equals( GunModel.this.leftArmAnimationChannel )
							|| channel.equals( GunModel.this.rightArmAnimationChannel )
						) {
							// Move hands away so we do not see it.
							dst.set( 0F, 4096F, 0F );
						}
						else { super.getPos( channel, dst ); }
					}
				} );
			}
			
			// TODO: override animator for arms?
			
			@Override
			public void tickInHand( E equipped, EnumHand hand )
			{
				final GunModel< ?, ?, ?, ? > $this = GunModel.this;
				final EntityPlayerSP player = FMUMClient.MC.player;
				final PlayerPatchClient patch = PlayerPatchClient.instance;
				final boolean crouching = player.isSneaking();
				final boolean sprinting = player.isSprinting();
				final boolean aiming = Key.AIM_HOLD.down;
				final Vec3f vec = Vec3f.locate();
				final Mat4f mat = Mat4f.locate();
				
				/// *** Get motion of the player. *** ///
				vec.set( patch.playerVelocity );
				vec.y = 0F;
				
				final float moveSpeed = player.onGround ? vec.length() : 0F;
				final float walkCycle = crouching ? $this.crouchWalkCycle : $this.walkCycle;
				walkDistanceCycle += moveSpeed * walkCycle;
				
				// TODO: Check drop impact
				
				/// *** Camera acceleration impact. *** ///
				// TODO: camera control
				
				/// *** Weapon acceleration impact. *** ///
				{
					// Transform acceleration into walk direction space.
					patch.camera.getPlayerRot( vec );
					mat.setIdentity();
					mat.rotateX( -vec.x );
					mat.rotateY(  vec.y );
					
					vec.set( patch.playerAcceleration );
					mat.transformAsPoint( vec );
					
					// Apply gun body shift.
					final Vec3f ip = $this.motionInertiaPos;
					final Vec3f ir = $this.motionInertiaRot;
					this.holdPos.velocity.add( vec.x * ip.x, vec.y * ip.y, vec.z * ip.z );
					this.holdRot.velocity.add( vec.y * ir.x, vec.x * ir.y, vec.x * ir.z );
				}
				
				/// *** Weapon walk/sprint bobbing. *** ///
				{
					vec.set( patch.playerVelocity );
					mat.transformAsPoint( vec );
					
					final float cycle = walkDistanceCycle + 0.5F * Util.PI;
					final float cos = moveSpeed * MathHelper.cos( cycle );
					final float sin = Math.abs( cos ) - 0.5F * moveSpeed;
					final Vec3f ap = sprinting ? $this.sprintAmplPos : $this.walkAmplPos;
					final Vec3f ar = sprinting ? $this.sprintAmplRot : $this.walkAmplRot;
					this.holdPos.velocity.add( cos * ap.x, sin * ap.y, cos * ap.z );
					this.holdRot.velocity.add( sin * ar.x, cos * ar.y, cos * ar.z );
				}
				
				/// *** Smooth on view rotation. *** ///
				{
					final float deltaPitch = player.rotationPitch - prevPlayerPitch;
					final float deltaYaw   = player.rotationYaw - prevPlayerYaw;
					final Vec3f ip = $this.viewInertiaPos;
					final Vec3f ir = $this.viewInertiaRot;
					this.holdPos.velocity.add( deltaYaw * ip.x, deltaPitch * ip.y, 0F );
					this.holdRot.velocity.add(
						deltaPitch * ir.x,
						deltaYaw   * ir.y,
						deltaYaw   * ir.z
					);
					
					// Do not forget to update previous pitch and yaw.
					prevPlayerPitch = player.rotationPitch;
					prevPlayerYaw = player.rotationYaw;
				}
				
				/// *** Setup target position and update. *** ///
				{
					final Vec3f tarPos = this.holdPos.tarPos;
					tarPos.set(
						aiming ? $this.aimPos :
						sprinting ? $this.sprintPos :
						crouching ? $this.crouchPos :
						$this.holdPos
					);
					
					final Vec3f moveOffset = (
						aiming ? $this.aimMoveOffset :
						sprinting ? $this.sprintMoveOffset :
						crouching ? $this.crouchMoveOffset :
						$this.moveOffset
					);
					final MovementInput input = player.movementInput;
					final boolean moving = (
						input.forwardKeyDown
						|| input.backKeyDown
						|| input.leftKeyDown
						|| input.rightKeyDown
					);
					tarPos.add( moving ? moveOffset : Vec3f.ORIGIN );
					
					// TODO: maybe move to outer layer and avoid the delay introduce by motion tendency
					final Vec3f shoulderOffset = (
						aiming ? $this.aimShoulderOffset :
						sprinting ? $this.sprintShoulderOffset :
						crouching ? $this.crouchShoulderOffset :
						$this.shoulderOffset
					);
					patch.camera.getPlayerRot( vec ); // TODO: used twice
					tarPos.scaleAdd( vec.x, shoulderOffset, tarPos );
					
					this.holdPos.update(
						$this.holdPosForceMult,
						$this.holdPosMaxForce,
						$this.holdPosDampingFactor
					);
				}
				
				/// *** Setup target rotation and update. *** ///
				{
					final Vec3f tarRot = this.holdRot.tarPos;
					tarRot.set(
						aiming ? $this.aimRot :
						sprinting ? $this.sprintRot :
						crouching ? $this.crouchRot :
						$this.holdRot
					);
					
					this.holdRot.update(
						$this.holdRotForceMult,
						$this.holdRotMaxForce,
						$this.holdRotDampingFactor
					);
				}
				
				mat.release();
				vec.release();
			}
			
			@Override
			protected void doRenderInHandSP( E equipped, EnumHand hand )
			{
				// Re-setup projection matrix.
				GL11.glMatrixMode( GL11.GL_PROJECTION );
				GL11.glLoadIdentity();
				Project.gluPerspective(
					GunModel.this.getFovModifier( this.smoother() ),
					( float ) FMUMClient.MC.displayWidth / FMUMClient.MC.displayHeight,
					0.05F, // TODO: maybe smaller this value to avoid seeing through the parts
					FMUMClient.SETTINGS.renderDistanceChunks * 16 * MathHelper.SQRT_2
				);
				GL11.glMatrixMode( GL11.GL_MODELVIEW );
				
				/* For arm adjust */
//				if ( Dev.flag )
//				{
//					GL11.glTranslatef( 0F, 4F / 16f, 15f / 16f );
//					
//					final EntityPlayer player = FMUMClient.MC.player;
//					GL11.glRotatef( -player.rotationPitch, 1F, 0F, 0F );
//					GL11.glRotatef( player.rotationYaw, 0F, 1F, 0F );
//					
//					GL11.glTranslatef( 0F, 0F, -5F/16f );
//				}
				
				final IAnimator animator = equipped.animator();
				equipped.setupRenderArm( animator, this.leftArm, this.rightArm );
				this.leftArm.updateArmOrientation();
				this.rightArm.updateArmOrientation();
				
				this.renderArm( animator, this.leftArm, GunModel.this.leftArmAnimationChannel );
				this.renderArm( animator, this.rightArm, GunModel.this.rightArmAnimationChannel );
				
				super.doRenderInHandSP( equipped, hand );
			}
			
			protected void renderArm( IAnimator animator, ArmTracker arm, String channel )
			{
				GL11.glPushMatrix();
				
				final Mat4f mat = Mat4f.locate();
				mat.setIdentity();
				animator.getChannel( CHANNEL_ITEM, mat );
				animator.applyChannel( channel, mat );
				
				final float alpha = 1F - animator.getFactor( channel );
				final Vec3f pos = Vec3f.locate();
				mat.get( pos );
				pos.interpolate( arm.handPos, alpha );
				
				final Quat4f rot = Quat4f.locate();
				final Quat4f gripRot = Quat4f.locate();
				gripRot.set( arm.handRot );
				rot.set( mat );
				rot.interpolate( gripRot, alpha );
				gripRot.release();
				
				mat.setIdentity();
				mat.translate( pos );
				mat.rotate( rot );
				rot.release();
				pos.release();
				
				glMulMatrix( mat );
				mat.release();
				
				this.bindTexture( TEXTURE_ALEX );
				
//				GL11.glEnable( GL11.GL_BLEND );
//				GL11.glColor4f( 1F, 1F, 1F, 0.5F );
				ALEX_ARM.render();
//				GL11.glDisable( GL11.GL_BLEND );
				
				GL11.glPopMatrix();
			}
			
			@Override
			protected void updatePosRot()
			{
				final float smoother = this.smoother();
				
				// Use #pos temporarily to avoid locate Vec3f.
				this.holdRot.get( smoother, this.pos );
				this.rot.set( this.pos );
				this.holdPos.get( smoother, this.pos );
			}
		}
	}
}
