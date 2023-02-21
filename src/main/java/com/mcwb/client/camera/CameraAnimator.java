package com.mcwb.client.camera;

import com.mcwb.client.IAutowireSmoother;
import com.mcwb.client.MCWBClient;
import com.mcwb.client.input.InputHandler;
import com.mcwb.client.player.PlayerPatchClient;
import com.mcwb.common.ModConfig;
import com.mcwb.util.BasedMotionTendency;
import com.mcwb.util.MotionTendency;
import com.mcwb.util.Vec3f;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.MouseHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * This implementation includes the critical code that makes free view work. Hence it is recommended
 * to implement your camera controller from this class. This also provides some simple camera
 * effects that can be tuned via {@link ModConfig}.
 * FIXME: 左右 alt 停住归中无抖动 上下有
 * @author Giant_Salted_Fish
 */
@SideOnly( Side.CLIENT )
public class CameraAnimator implements ICameraController, IAutowireSmoother
{
	public static final CameraAnimator INSTANCE = new CameraAnimator();
	
	protected static final float VIEW_SHIFTER = Float
		.intBitsToFloat( ( Float.floatToIntBits( 90F ) >>> 23 ) - 23 << 23 );
	
	/**
	 * This is static and shared for all its sub-type instances to make sure the cycle will not jump
	 * when switching the camera animator
	 */
	protected static float dropDistanceCycle = 0F;
	
	/**
	 * The actual camera rotation in world coordinate(player rot + off-axis)
	 */
	protected final Vec3f camRot = new Vec3f();
	
	/**
	 * Direction that the player is heading
	 */
	protected final Vec3f playerRot = new Vec3f();
	
	/**
	 * Off-axis view angle that triggered by free view
	 */
	protected final MotionTendency camOffAxis = new BasedMotionTendency( 0.4F, 50F, 0.125F );
	
	/**
	 * Handles easing animation on camera. For example the drop camera shake.
	 */
	protected final MotionTendency camEasing = new BasedMotionTendency( 0.4F, 4.25F, 1F );
	
	/**
	 * Handles the camera effects
	 */
	@Override
	public void tick()
	{
		// If looking around, clear camera recover speed
		if( InputHandler.FREE_VIEW.down || InputHandler.CO_FREE_VIEW.down )
			this.camOffAxis.velocity.setZero();
		
		// Otherwise, update off-axis rotation
		else this.camOffAxis.update();
		
		/// Apply drop camera effects ///
		final PlayerPatchClient patch = PlayerPatchClient.instance;
		
		final float dropSpeed = Math.min( 0F, patch.playerVelocity.y );
		dropDistanceCycle += dropSpeed * MCWBClient.camDropCycle;
		
		this.camEasing.velocity.z += dropSpeed * MCWBClient.camDropAmpl
			* MathHelper.sin( dropDistanceCycle );
		
		// Apply drop impact on camera if upon hitting the ground
		if( patch.prevPlayerVelocity.y < 0F && patch.playerAcceleration.y > 0F )
		{
			final Vec3f headVelocity = this.camEasing.velocity;

//			boolean positive = MCWB.rand.nextBoolean();
			// Make sure the drop impact always makes the head tilt harder on its original direction
			boolean positive = headVelocity.z > 0F;
			if( positive ^ this.camEasing.curPos.z > 0F )
			{
				headVelocity.z = -headVelocity.z;
				positive = !positive;
			}
			
			// Value of cap#playerAcceleration#y has to be positive here
			final float impact = patch.playerAcceleration.y * MCWBClient.camDropImpact;
			headVelocity.z += positive ? impact : -impact;
		}
		
		this.camEasing.update();
	}
	
	/**
	 * Handles the view update upon mouse input
	 */
	@Override
	public void prepareRender( MouseHelper mouse )
	{
		final EntityPlayerSP player = MCWBClient.MC.player;
		final MotionTendency camOffAxis = this.camOffAxis;
		final float smoother = this.smoother();
		
		// Apply a tiny change to player's view to force chunk load
		player.rotationPitch += VIEW_SHIFTER;
		player.prevRotationPitch += VIEW_SHIFTER;
		
		// Get input mouse delta
		final float mouseFactor = this.getMouseFactor();
		final float deltaYaw = mouse.deltaX * mouseFactor;
		
		// Make sure delta pitch is inside the limit
		final Vec3f camRot = this.camRot;
		camRot.x = player.rotationPitch + camOffAxis.getX( smoother );
		
		final float deltaY = mouse.deltaY * mouseFactor;
		final float deltaPitch = MathHelper.clamp(
			MCWBClient.SETTINGS.invertMouse ? deltaY : -deltaY,
			-90F - camRot.x,
			90F - camRot.x
		);
		camRot.x += deltaPitch;
		
		// If looking around, apply view rot to off-axis
		if( InputHandler.FREE_VIEW.down || InputHandler.CO_FREE_VIEW.down )
		{
			this.playerRot.set( player.rotationPitch, player.rotationYaw, 0F );
			
			final Vec3f offAxis = camOffAxis.curPos;
			// This is commented as the state of look around key is updated by tick so there is no \
			// way it can change in between the ticks. And may be the key update event is earlier \
			// than the item tick hence set it with smoothed value will actually cause that view \
			// jump effect. TODO: This may have to be add back once tick method of item is moved \
			// to Item#update( ... ) method.
//			camOffAxis.smoothedPos( offAxis, smoother );
			
			// Apply delta rot and make the rotation would not exceed the limit
			final float yawLimit = MathHelper
				.sqrt( MCWBClient.freeViewLimitSquared - camRot.x * camRot.x );
			offAxis.y = MathHelper.clamp( offAxis.y + deltaYaw , -yawLimit, yawLimit );
			offAxis.x += deltaPitch;
			
			// Set previous with current value to avoid bobbing
			camOffAxis.prevPos.set( offAxis );
			
			// Clear mouse input to avoid changing walking direction
			mouse.deltaX = mouse.deltaY = 0;
		}
		else this.playerRot
			.set( player.rotationPitch + deltaPitch, player.rotationYaw + deltaYaw, 0F );
		
		// Do not forget to update camera rot yaw
		camRot.y = this.playerRot.y + camOffAxis.getY( smoother );
	}
	
	@Override
	public void getCameraRot( Vec3f dst )
	{
		this.camEasing.getPos( dst, this.smoother() );
		dst.add( this.camRot );
	}
	
	@Override
	public void getPlayerRot( Vec3f dst ) { dst.set( this.playerRot ); }
	
	protected final float getMouseFactor()
	{
		final float factor = MCWBClient.SETTINGS.mouseSensitivity * 0.6F + 0.2F;
		return factor * factor * factor * 8F * 0.15F;
	}
}
