package com.fmum.client.camera;

import com.fmum.client.FMUMClient;
import com.fmum.client.input.InputManager;
import com.fmum.client.input.Inputs;
import com.fmum.client.player.PlayerPatchClient;
import com.fmum.util.DynamicPos;
import com.fmum.util.Mat4f;
import com.fmum.util.Vec3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.MouseHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class CameraController implements ICameraController
{
	public static final CameraController INSTANCE = new CameraController();
	
	public static final String ANI_CHANNEL = "camera";
	
	protected static final float PITCH_JITTER = Float
		.intBitsToFloat( ( Float.floatToIntBits( 90F ) >>> 23 ) - 23 << 23 );
	
	protected static float drop_distance_cycle = 0.0F;
	
	protected final Mat4f view_mat = new Mat4f();
	
	protected final Vec3f player_rot = new Vec3f();
	
	protected final DynamicPos free_view_rot = new DynamicPos();
	
	protected final DynamicPos camera_easing = new DynamicPos();
	
	@Override
	public void tick()
	{
		if ( InputManager.getInput( Inputs.FREE_VIEW ).asBool() )
		{
			// Clear camera recover speed when looking around.
			this.camera_easing.velocity.setZero();
		}
		else
		{
			// Otherwise, update off-axis rotation.
			this.free_view_rot.update( 0.125F, 50.0F, 0.4F );
		}
		
		// Apply camera drop effect.
		final PlayerPatchClient player = PlayerPatchClient.instance;
		
		final float drop_speed = Math.min( 0.0F, player.velocity.y );
		drop_distance_cycle += drop_speed * FMUMClient.camera_drop_cycle;
		
		final float oscillation = drop_speed * MathHelper.sin( drop_distance_cycle );
		this.camera_easing.velocity.z += FMUMClient.camera_drop_amplitude * oscillation;
		
		final boolean is_touching_the_ground =
			player.prev_velocity.y < 0.0F && player.acceleration.y > 0.0F;
		if ( is_touching_the_ground )
		{
			final Vec3f easing_velocity = this.camera_easing.velocity;
			
			// The drop impact should always make the \
			// head tilt harder on the same direction.
			boolean is_positive = easing_velocity.z > 0.0F;
			if ( is_positive != this.camera_easing.curPos.z > 0.0F )
			{
				easing_velocity.z = -easing_velocity.z;
				is_positive = !is_positive;
			}
			
			// Value of player.acceleration#y has to be positive here.
			final float impact = player.acceleration.y * FMUMClient.camera_drop_impact;
			easing_velocity.z += is_positive ? impact : -impact;
		}
		
		this.camera_easing.update( 1.0F, 4.25F, 0.4F );
	}
	
	@Override
	public void prepareRender( MouseHelper mouse )
	{
		// Handles view update upon mouse input.
		final Minecraft mc = FMUMClient.MC;
		final EntityPlayerSP player = mc.player;
		final DynamicPos free_view_rot = this.free_view_rot;
		final Vec3f player_rot = this.player_rot;
		final float smoother = mc.getRenderPartialTicks();
		
		// Process input mouse delta.
		final float mouse_factor = this._getMouseFactor();
		final float mouse_delta_y =
			mc.gameSettings.invertMouse ? mouse.deltaY : -mouse.deltaY;
		final float raw_delta_pitch = mouse_delta_y * mouse_factor;
		final float raw_delta_yaw = mouse.deltaX * mouse_factor;
		
		// Make sure delta pitch is inside the limit.
		final float free_view_rot_x = free_view_rot.getX( smoother );
		final float raw_view_pitch = player.rotationPitch + free_view_rot_x;
		final float new_view_pitch = MathHelper.clamp(
			raw_view_pitch + raw_delta_pitch, -90.0F, 90.0F );
		final float delta_pitch = new_view_pitch - raw_view_pitch;
		
		// If looking around, apply view rot to off-axis.
		if ( InputManager.getInput( Inputs.FREE_VIEW ).asBool() )
		{
			player_rot.set( player.rotationPitch, player.rotationYaw, 0.0F );
			
			final Vec3f cur_fv_rot = free_view_rot.curPos;
			// This is commented as the state of look around key is updated by tick so there is no \
			// way it can change in between the ticks. And may be the key update event is earlier \
			// than the item tick hence set it with smoothed value will actually cause that view \
			// jump effect.
//			free_view_rot.smoothedPos( cur_fv_rot, smoother );
			cur_fv_rot.x += delta_pitch;
			
			// Make sure the yaw rotation would not exceed the limit.
			final float pitch_squared = new_view_pitch *  new_view_pitch;
			final float yaw_limit_squared = FMUMClient.free_view_limit_squared - pitch_squared;
			final float yaw_limit = MathHelper.sqrt( yaw_limit_squared );
			cur_fv_rot.y = MathHelper.clamp( cur_fv_rot.y + raw_delta_yaw, -yaw_limit, yaw_limit );
			
			// Set previous to current value to avoid bobbing.
			free_view_rot.prevPos.set( cur_fv_rot );
			
			// Clear mouse input to prevent walking direction change.
			mouse.deltaX = 0;
			mouse.deltaY = 0;
			
			this._updateViewRot( cur_fv_rot.x, cur_fv_rot.y );
		}
		else
		{
			final float pitch = player.rotationPitch + delta_pitch;
			final float yaw = player.rotationYaw + raw_delta_yaw;
			player_rot.set( pitch, yaw, 0.0F );
			this._updateViewRot( free_view_rot_x, free_view_rot.getY( smoother ) );
		}
	}
	
	protected final void _updateViewRot( float free_view_rot_x, float free_view_rot_y )
	{
		// Apply easing and camera animation.
		final float view_pitch = this.player_rot.x + free_view_rot_x;
		final float view_yaw = this.player_rot.y + free_view_rot_y;
	}
	
	protected final float _getMouseFactor()
	{
		final GameSettings settings = FMUMClient.MC.gameSettings;
		final float sensi = PlayerPatchClient
			.instance.getMouseSensitivity( settings.mouseSensitivity );
		final float factor = sensi * 0.6F + 0.2F;
		return factor * factor * factor * 8F * 0.15F;
	}
	
	@Override
	public void getViewMat( Mat4f dst ) {
		dst.set( this.view_mat );
	}
}
