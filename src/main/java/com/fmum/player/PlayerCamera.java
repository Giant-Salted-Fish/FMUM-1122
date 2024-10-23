package com.fmum.player;

import com.fmum.SyncConfig;
import com.fmum.input.InputManager;
import com.fmum.input.Inputs;
import gsf.util.animation.MassSpringMotion;
import gsf.util.math.Quat4f;
import gsf.util.math.Vec3f;
import gsf.util.render.IPose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.MouseHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public abstract class PlayerCamera implements IPlayerCamera
{
	protected static final float PITCH_JITTER;
	static
	{
		final float pitch_max = 90.0F;
		final int power = Float.floatToIntBits( pitch_max ) >>> 23;
		final int min_power = power - 23;
		PITCH_JITTER = Float.intBitsToFloat( min_power << 23 );
	}
	
	
	/**
	 * Shared drop distance cycle for different camera implementations.
	 */
	protected static float drop_distance_cycle = 0.0F;
	
	
	protected final Vec3f player_rot = new Vec3f();
	
	protected final MassSpringMotion free_view_rot = new MassSpringMotion();
	protected final MassSpringMotion camera_easing = new MassSpringMotion();
	
	protected IPose camera_pose = IPose.EMPTY;
	
	@Override
	public void tickCamera()
	{
		// TODO: Buffer this input so no need to re-fetch every frame.
		if ( InputManager.getBoolState( Inputs.FREE_VIEW ) )
		{
			// Clear camera recover speed when looking around.
			this.free_view_rot.setVelocity( Vec3f.ORIGIN );
		}
		else
		{
			// Otherwise, update off-axis rotation.
			this.free_view_rot.update( 0.125F, 50.0F, 0.4F );
		}
		
		// Apply camera drop effect.
		final PlayerPatchClient player = PlayerPatchClient.get();
		
		final float drop_speed = Math.min( 0.0F, player.velocity.y );
		drop_distance_cycle += drop_speed * SyncConfig.camera_drop_cycle;
		
		final float oscillation = drop_speed * MathHelper.sin( drop_distance_cycle );
		this.camera_easing.offsetVelocity( 0.0F, 0.0F, oscillation * SyncConfig.camera_drop_amplitude );
		
		final boolean on_landing = player.prev_velocity.y < 0.0F && player.acceleration.y > 0.0F;
		if ( on_landing )
		{
			// The drop impact should always make the head tilt harder on the \
			// same direction.
			final Vec3f vec = Vec3f.allocate();
			this.camera_easing.getCurPos( vec );
			final boolean is_positive = vec.z > 0.0F;
			
			this.camera_easing.getVelocity( vec );
			if ( vec.z > 0.0F != is_positive ) {
				vec.z = -vec.z;
			}
			
			// Value of player.acceleration#y has to be positive here.
			final float impact = player.acceleration.y * SyncConfig.camera_drop_impact;
			vec.z += is_positive ? impact : -impact;
			this.camera_easing.setVelocity( vec );
			Vec3f.release( vec );
		}
		
		this.camera_easing.update( 1.0F, 4.25F, 0.4F );
	}
	
	@Override
	public void prepareRender( MouseHelper mouse )
	{
		// Handles view update upon mouse input.
		final Minecraft mc = Minecraft.getMinecraft();
		final EntityPlayerSP player = mc.player;
		final MassSpringMotion free_view_rot = this.free_view_rot;
		final Vec3f player_rot = this.player_rot;
		final float smoother = mc.getRenderPartialTicks();
		
		// Process input mouse delta.
		final float mouse_factor = this._getMouseFactor();
		final float mouse_delta_y = mc.gameSettings.invertMouse ? mouse.deltaY : -mouse.deltaY;
		final float raw_delta_pitch = mouse_delta_y * mouse_factor;
		final float raw_delta_yaw = mouse.deltaX * mouse_factor;
		
		// Make sure delta pitch is inside the limit.
		final float free_view_pitch = free_view_rot.getPosX( smoother );
		final float raw_view_pitch = player.rotationPitch + free_view_pitch;
		final float new_view_pitch = MathHelper.clamp( raw_view_pitch + raw_delta_pitch, -90.0F, 90.0F );
		final float delta_pitch = new_view_pitch - raw_view_pitch;
		
		// If looking around, apply view rot to off-axis.
		if ( InputManager.getBoolState( Inputs.FREE_VIEW ) )
		{
			player_rot.set( player.rotationPitch, player.rotationYaw, 0.0F );
			
			final Vec3f cur_fv_rot = Vec3f.allocate();
			free_view_rot.getCurPos( cur_fv_rot );
			// This is commented as the state of look around key is updated by \
			// tick so there is no way it can change in between the ticks. And \
			// may be the key update event is earlier than the item tick hence \
			// set it with smoothed value will actually cause that view jump effect.
//			free_view_rot.smoothedPos( cur_fv_rot, smoother );
			cur_fv_rot.x += delta_pitch;
			
			// Make sure the yaw rotation would not exceed the limit.
			final float pitch_squared = new_view_pitch *  new_view_pitch;
			final float yaw_limit_squared = SyncConfig.free_view_limit_squared - pitch_squared;
			final float yaw_limit = MathHelper.sqrt( yaw_limit_squared );
			final float yaw_rot = cur_fv_rot.y + raw_delta_yaw;
			cur_fv_rot.y = MathHelper.clamp( yaw_rot, -yaw_limit, yaw_limit );
			
			// Clear mouse input to prevent walking direction change.
			mouse.deltaX = 0;
			mouse.deltaY = 0;
			
			this._updateViewRot( cur_fv_rot.x, cur_fv_rot.y );
			
			// Set previous to current value to avoid bobbing.
			// Needs to set it after #_updateViewRot(...) call as it uses the \
			// DynPos#getPrevPos to check if free view rotation was updated.
			free_view_rot.resetPos( cur_fv_rot );
			Vec3f.release( cur_fv_rot );
		}
		else
		{
			final float pitch = player.rotationPitch + delta_pitch;
			final float yaw = player.rotationYaw + raw_delta_yaw;
			player_rot.set( pitch, yaw, 0.0F );
			this._updateViewRot( free_view_pitch, free_view_rot.getPosY( smoother ) );
		}
	}
	
	protected final void _updateViewRot( float free_view_pitch, float free_view_yaw )
	{
		// Apply easing and camera animation.
		final Minecraft mc = Minecraft.getMinecraft();
		final float view_pitch = this.player_rot.x + free_view_pitch;
		final float view_yaw = this.player_rot.y + free_view_yaw;
		
//		final IPose setup = this.animator.getChannel( CHANNEL_CAMERA );
		
//		final Mat4f view_mat = this.view_mat;
//		final Quat4f quat = Quat4f.allocate();
//		setup.getRot( quat );
//		view_mat.set( quat );
//		Quat4f.release( quat );
		final Quat4f quat = new Quat4f();
		
		final Vec3f vec = Vec3f.allocate();
		this.camera_easing.getPos( mc.getRenderPartialTicks(), vec );
		quat.setRotZ( vec.z );
		quat.rotateX( vec.x );
		quat.rotateY( vec.y );
		
//		setup.getPos( vec );
//		view_mat.translate( vec );
//		Vec3f.release( vec );
		
		quat.rotateX( view_pitch );
		quat.rotateY( 180.0F + view_yaw );
		this.camera_pose = IPose.of( Vec3f.ORIGIN, quat );
		
		// Apply a tiny change to player's pitch rotation to force view \
		// frustum culling update if off-axis has changed.
		final EntityPlayerSP player = mc.player;
		final Vec3f prev_free_view_rot = Vec3f.allocate();
		this.free_view_rot.getPrevPos( prev_free_view_rot );
		final float pitch_change = Math.abs( free_view_pitch - prev_free_view_rot.x );
		final float yaw_change = Math.abs( free_view_yaw - prev_free_view_rot.y );
		Vec3f.release( prev_free_view_rot );
		
		// Filter jitter to prevent view frustum culling update on subtle \
		// oscillation of the DynPos system.
		final float raw_jitter = pitch_change + yaw_change;
		if ( raw_jitter >= PITCH_JITTER )
		{
			player.rotationPitch += PITCH_JITTER;
			player.prevRotationPitch += PITCH_JITTER;
		}
	}
	
	@Override
	public IPose getCameraSetup() {
		return this.camera_pose;
	}
	
	protected float _getMouseFactor()
	{
		final float sensitivity = this._getMouseSensitivity();
		final float factor = sensitivity * 0.6F + 0.2F;
		return factor * factor * factor * 8.0F * 0.15F;
	}
	
	protected abstract float _getMouseSensitivity();
}
