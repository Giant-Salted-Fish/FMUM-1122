package com.fmum.client.camera;

import com.fmum.client.FMUMClient;
import com.fmum.client.input.Key;
import com.fmum.client.player.PlayerPatchClient;
import com.fmum.client.render.IAnimator;
import com.fmum.common.ModConfig;
import com.fmum.util.DynamicPos;
import com.fmum.util.Mat4f;
import com.fmum.util.Quat4f;
import com.fmum.util.Vec3f;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MouseHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * This implementation includes the critical code that makes free view work. Hence, it is recommended
 * to implement your camera controller from this class. This also provides some simple camera
 * effects that can be tuned via {@link ModConfig}.
 * 
 * @author Giant_Salted_Fish
 */
@SideOnly( Side.CLIENT )
public class CameraAnimator implements ICameraController
{
	public static final CameraAnimator INSTANCE = new CameraAnimator();
	
	public static final String ANIMATION_CAHNNEL = "camera";
	
	protected static final float PITCH_SHIFTER = Float
		.intBitsToFloat( ( Float.floatToIntBits( 90F ) >>> 23 ) - 23 << 23 );
	
	/**
	 * This is static and shared for all its subtype instances to make sure the cycle will not jump
	 * when switching the camera animator.
	 */
	protected static float dropDistanceCycle = 0F;
	
	/**
	 * The actual camera orientation and position in world coordinate.
	 */
	protected final Mat4f viewMat = new Mat4f();
	
	/**
	 * Direction that the player is heading.
	 */
	protected final Vec3f playerRot = new Vec3f();
	
	/**
	 * Off-axis view angle that triggered by free view.
	 */
	protected final DynamicPos cameraOffAxis = new DynamicPos();
	protected final Vec3f prevCameraOffAxis = new Vec3f();
	
	/**
	 * Handles easing animation on camera. For example the drop camera shake.
	 */
	protected final DynamicPos cameraEasing = new DynamicPos();
	
	protected IAnimator animation = IAnimator.NONE;
	
	@Override
	public void tick()
	{
		// If looking around, clear camera recover speed.
		if ( Key.FREE_VIEW.down || Key.CO_FREE_VIEW.down )
			this.cameraOffAxis.velocity.setZero();
		
		// Otherwise, update off-axis rotation.
		else this.cameraOffAxis.update( 0.125F, 50F, 0.4F );
		
		/// *** Apply drop camera effects. *** ///
		final PlayerPatchClient patch = PlayerPatchClient.instance;
		final Vec3f playerVelo = patch.prevPlayerVelocity;
		final Vec3f playerAcc = patch.playerAcceleration;
		
		final float dropSpeed = Math.min( 0F, playerVelo.y );
		dropDistanceCycle += dropSpeed * FMUMClient.camDropCycle;
		
		final float oscillation = dropSpeed * MathHelper.sin( dropDistanceCycle );
		this.cameraEasing.velocity.z += FMUMClient.camDropAmpl * oscillation;
		
		final boolean touchingTheGround = playerVelo.y < 0F && playerAcc.y > 0F;
		if ( touchingTheGround )
		{
			final Vec3f headVelocity = this.cameraEasing.velocity;
			
			// The drop impact should always make the head tilt harder on its original direction.
			boolean positive = headVelocity.z > 0F;
			if ( positive ^ this.cameraEasing.curPos.z > 0F )
			{
				headVelocity.z = -headVelocity.z;
				positive = !positive;
			}
			
			// Value of playerAcc#y has to be positive here.
			final float impact = playerAcc.y * FMUMClient.camDropImpact;
			headVelocity.z += positive ? impact : -impact;
		}
		
		this.cameraEasing.update( 1F, 4.25F, 0.4F );
	}
	
	@Override
	public void useAnimation( IAnimator animation ) { this.animation = animation; }
	
	// Handles view update upon mouse input.
	@Override
	public void prepareRender( MouseHelper mouse )
	{
		final EntityPlayer player = FMUMClient.MC.player;
		final DynamicPos camOffAxis = this.cameraOffAxis;
		final Vec3f playerRot = this.playerRot;
		final float smoother = FMUMClient.smoother();
		
		// Process input mouse delta.
		final float mouseFactor = this.getMouseFactor();
		final float mouseDeltaY = FMUMClient.SETTINGS.invertMouse ? mouse.deltaY : -mouse.deltaY;
		final float rawDeltaPitch = mouseDeltaY * mouseFactor;
		final float rawDeltaYaw = mouse.deltaX * mouseFactor;
		
		// Make sure delta pitch is inside the limit.
		final float camOffAxisX = camOffAxis.getX( smoother );
		final float rawCameraPitch = player.rotationPitch + camOffAxisX;
		final float newCameraPitch = MathHelper.clamp( rawCameraPitch + rawDeltaPitch, -90F, 90F );
		final float deltaPitch = newCameraPitch - rawCameraPitch;
		
		// If looking around, apply view rot to off-axis.
		if ( Key.FREE_VIEW.down || Key.CO_FREE_VIEW.down )
		{
			playerRot.set( player.rotationPitch, player.rotationYaw, 0F );
			
			final Vec3f offAxis = camOffAxis.curPos;
			// This is commented as the state of look around key is updated by tick so there is no \
			// way it can change in between the ticks. And may be the key update event is earlier \
			// than the item tick hence set it with smoothed value will actually cause that view \
			// jump effect.
//			camOffAxis.smoothedPos( offAxis, smoother );
			offAxis.x += deltaPitch;
			
			// Make the yaw rotation would not exceed the limit.
			final float pitchSquared = newCameraPitch * newCameraPitch;
			final float yawLimitSquared = FMUMClient.freeViewLimitSquared - pitchSquared;
			final float yawLimit = MathHelper.sqrt( yawLimitSquared );
			offAxis.y = MathHelper.clamp( offAxis.y + rawDeltaYaw, -yawLimit, yawLimit );
			
			// Set previous with current value to avoid bobbing.
			camOffAxis.prevPos.set( offAxis );
			
			// Clear mouse input to prevent walking direction change.
			mouse.deltaX = 0;
			mouse.deltaY = 0;
			
			this.updateCameraRot( offAxis.x, offAxis.y );
		}
		else
		{
			final float pitch = player.rotationPitch + deltaPitch;
			final float yaw   = player.rotationYaw   + rawDeltaYaw;
			playerRot.set( pitch, yaw, 0F );
			this.updateCameraRot( camOffAxisX, camOffAxis.getY( smoother ) );
		}
	}
	
	protected final void updateCameraRot( float camOffAxisX, float camOffAxisY )
	{
		// Apply easing and camera animation.
		final float cameraPitch = this.playerRot.x + camOffAxisX;
		final float cameraYaw   = this.playerRot.y + camOffAxisY;
		
		this.animation.update();
		
		final Mat4f mat = this.viewMat;
		final Quat4f quat = Quat4f.locate();
		this.animation.getRot( ANIMATION_CAHNNEL, quat );
		mat.set( quat );
		quat.release();
		
		final Vec3f vec = Vec3f.locate();
		this.cameraEasing.get( FMUMClient.smoother(), vec );
		mat.rotateZ( vec.z );
		mat.rotateX( vec.x );
		mat.rotateY( vec.y );
		
		this.animation.getPos( ANIMATION_CAHNNEL, vec );
		mat.translate( vec );
		vec.release();
		
		mat.rotateX( cameraPitch );
		mat.rotateY( 180F + cameraYaw );
		
		// Apply a tiny change to player's pitch rotation to force view frustum culling update if
		// off-axis has changed.
		final EntityPlayer player = FMUMClient.MC.player;
		final Vec3f prevOffAxis = this.prevCameraOffAxis;
		final float pitchChange = Math.abs( camOffAxisX - prevOffAxis.x );
		final float yawChange   = Math.abs( camOffAxisY - prevOffAxis.y );
		
		final float rawShifter = Math.min( PITCH_SHIFTER, pitchChange + yawChange );
		final float clampedShifter = rawShifter < PITCH_SHIFTER ? 0F : rawShifter;
		player.rotationPitch += clampedShifter;
		player.prevRotationPitch = player.rotationPitch;
		
		prevOffAxis.x = camOffAxisX;
		prevOffAxis.y = camOffAxisY;
	}
	
	@Override
	public void getViewTransform( Mat4f dst ) { dst.set( this.viewMat ); }
	
	@Override
	public void getPlayerRot( Vec3f dst ) { dst.set( this.playerRot ); }
	
	protected final float getMouseFactor()
	{
		final float factor = FMUMClient.SETTINGS.mouseSensitivity * 0.6F + 0.2F;
		return factor * factor * factor * 8F * 0.15F;
	}
}
