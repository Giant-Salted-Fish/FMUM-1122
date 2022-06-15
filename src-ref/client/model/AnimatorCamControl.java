package com.fmum.client.model;

import org.lwjgl.opengl.GL11;

import com.fmum.client.FMUMClient;
import com.fmum.client.KeyManager.Key;
import com.fmum.common.FMUM;
import com.fmum.common.type.TypeInfo;
import com.fmum.common.util.MotionTendencyBased;
import com.fmum.common.util.CoordSystem;
import com.fmum.common.util.Util;
import com.fmum.common.util.MotionTracks;
import com.fmum.common.util.Vec3;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MouseHelper;

public class AnimatorCamControl extends Animator
{
	public static final AnimatorCamControl INSTANCE = new AnimatorCamControl();
	
	/**
	 * @note This value is not real-time
	 */
	protected static final Vec3 prevCamPos = Vec3.get();
	
	protected static final Vec3
		camVelocity = Vec3.get(),
		prevCamVelocity = Vec3.get();
	
	// TODO: read from config
	public static double
		dropCycle = Math.PI * 0.3D,
		dropAmpltCam = 3D;
	
	public static double
		dropImpactAmpltCam = 7.5D;
	
	protected static double dropDistanceCycle = 0D;
	
	protected static float viewPitchShifter = Float.intBitsToFloat(
		(Float.floatToIntBits(90F) >>> 23) - 23 << 23
	);
	
	/**
	 * Player's actual rotation when rendering scope glass texture. Note that x value should be 0D
	 * if is not tilting the body.
	 */
	public final Vec3 actualPlayerRot = Vec3.get();
	
	public final Vec3
		renderCamPos = Vec3.get(),
		renderCamRot = Vec3.get();
	
	/**
	 * Used to save the off-axis angle when {@link Key#lookAroundActivated()} is {@code true}
	 */
	public final MotionTendencyBased eyeOffAxis = new MotionTendencyBased(0.4D, 40D, 0.125D);
	
	/**
	 * Animation tracks to control the camera. Notice that this will not effect the actual walking
	 * direction of the player.
	 */
	public final MotionTracks<MotionTendencyBased>
		camOffAxis = new MotionTracks<>(
			new MotionTendencyBased(0.4D, 4.25D, 1D),
			new MotionTendencyBased(0.4D, 4.25D, 1D)
		);
	
	public float
		headRotYawLimitBase = 30F,
		headRotYawLimitRange = 80F;
	
	public final Vec3
		renderPos = Vec3.get(),
		renderRot = Vec3.get();
	
	// TODO: camera effects update in itemTick
	
	/**
	 * Updates the actual camera orientation
	 */
	@Override
	public void itemRenderTick(ItemStack stack, TypeInfo type, MouseHelper mouse)
	{
		final float smoother = getSmoother();
		final EntityPlayerSP player = getPlayer();
		final Vec3 actual = this.actualPlayerRot;
		actual.set(0D, player.rotationYaw, player.rotationPitch);
		
		final Vec3 eyePos = this.renderCamPos;
		final Vec3 eyeRot = this.renderCamRot;
		
		// Update player's actual eye position
		double prev = player.prevPosX;
		eyePos.x = prev + (player.posX - prev) * smoother;
		prev = player.prevPosY;
		eyePos.y = prev + (player.posY - prev) * smoother + player.getEyeHeight();
		prev = player.prevPosZ;
		eyePos.z = prev + (player.posZ - prev) * smoother;
		
		/// Handle view rotation ///
		// Get mouse sensitivity
		float mouseSensi = FMUMClient.settings.mouseSensitivity * 0.6F + 0.2F;
		mouseSensi *= mouseSensi * mouseSensi * 8F * 0.15F;
		
		float changeYaw = mouse.deltaX * mouseSensi;
		float changePitch = mouse.deltaY * mouseSensi * (getInvertMouse() ? 1F : -1F);
		
		// If looking around, apply view rotate to eye rotation shift
		final boolean lookingAround = Key.lookAroundActivated();
		
		if(lookingAround)
		{
			double pitchLimitMin = -90D - actual.z;
			double pitchLimitMax = 90D - actual.z;
			final MotionTendencyBased offAsix = this.eyeOffAxis;
			eyeRot.set(actual);
			
			Vec3 v = offAsix.curPos;
			v.trans(0D, changeYaw, changePitch);
			v.z = Util.clamp(v.z, pitchLimitMin, pitchLimitMax);
			eyeRot.z += v.z;
			
			// Use updated pitch value to calculate yaw limit to avoid bobbing
			double yawLimit = this.headRotYawLimitBase
				+ this.headRotYawLimitRange * (90D - Math.abs(eyeRot.z)) / 90D;
			v.y = Util.clamp(v.y, -yawLimit, yawLimit);
			eyeRot.y += v.y;
			
			eyeRot.x += v.x;
			
			// Set previous with current position to avoid bobbing
			offAsix.prevPos.set(v);
			
			// Clear mouse input to avoid changing walking direction
			mouse.deltaX = mouse.deltaY = 0;
		}
		else
		{
			actual.trans(0D, changeYaw, changePitch);
			actual.z = Util.clamp(actual.z, -90D, 90D);

			eyeRot.set(actual);
			this.eyeOffAxis.getSmoothedPos(vec, smoother);
			eyeRot.trans(vec);
		}
		
		this.camOffAxis.getSmoothedPos(vec, smoother);
		eyeRot.trans(vec);
		eyeRot.z = Util.clamp(eyeRot.z, -90D, 90D);
		
		setRenderCamRoll((float)eyeRot.x);
		setRenderCamYaw((float)eyeRot.y + 180F);
		setRenderCamPitch((float)eyeRot.z);
		
		// Call shoulder coordinate system update
		this.updateRenderPosRot(stack, type);
		
		// Apply a tiny change to player's view to force view chunk update
		viewPitchShifter = (
			actual.z == 90F
			? -Math.abs(viewPitchShifter)
			: (
				actual.z == -90F
				? Math.abs(viewPitchShifter)
				: -viewPitchShifter
			)
		);
		player.rotationPitch += viewPitchShifter;
		player.prevRotationPitch += viewPitchShifter;
	}
	
	@Override
	public void itemTick(ItemStack stack, TypeInfo type)
	{
		final MotionTendencyBased offAxis = this.eyeOffAxis;
		
		// If looking around, clear camera recover speed
		if(Key.lookAroundActivated())
			offAxis.velocity.set(0D);
		
		// Otherwise, update render off-axis rotation
		else offAxis.update();
		
		this.doItemTick(stack, type);
	}
	
	public void restoreGLWorldCoordSystem(float smoother)
	{
		// Restore world coordinate system
		Vec3 v = this.renderCamRot;
		GL11.glRotated(-v.x, 1D, 0D, 0D);
		GL11.glRotated(v.z, 0D, 0D, 1D);
		GL11.glRotated(v.y + 90D, 0D, 1D, 0D);
		
		v = this.renderCamPos;
		GL11.glTranslated(-v.x, -v.y, -v.z);
	}
	
	/**
	 * Setup the coordinate system that this item should located in when rendering. In default it
	 * just setup the system with {@link #renderPos} and {@link #renderRot}.
	 * 
	 * @hackCode
	 *     In most cases this will be called to setup the coordinate system. hence in this
	 *     implementation it calls {@link CoordSystem#globalTrans(Vec3)} and
	 *     {@link CoordSystem#globalRot(double, double, double)} to do the transformation. If this
	 *     condition is not always true in your implementation, override and fall back to standard
	 *     transform {@link CoordSystem} functions.
	 * @param sys
	 *     Coordinate system to load render coordinate system into. Note that the input system could
	 *     be a raw system so it is recommended to call {@link CoordSystem#setDefault()} before
	 *     apply any transform.
	 */
	public void setupRenderTransform(CoordSystem sys, float smoother)
	{
		sys.globalTrans(this.renderPos);
		
		Vec3 v = this.renderRot;
		sys.globalRot(v.x, -v.y - 90D, -v.z);
	}
	
	public void setupGLRenderTransform(float smoother)
	{
		Vec3 v = this.renderPos;
		GL11.glTranslated(v.x, v.y, v.z);
		
		v = this.renderRot;
		GL11.glRotated(-v.y - 90D, 0D, 1D, 0D);
		GL11.glRotated(-v.z, 0D, 0D, 1D);
		GL11.glRotated(v.x, 1D, 0D, 0D);
	}
	
	protected void doItemTick(ItemStack stack, TypeInfo type)
	{
		final MotionTendencyBased easingCam = this.camOffAxis.grab(MotionTracks.EASING);
		
		/// Apply drop camera effects ///
		updateCamVelocity();
		vec.set(camVelocity);
		double dropSpeed = Math.min(0D, vec.y);
		dropDistanceCycle += dropSpeed * dropCycle;
		
		vec.sub(prevCamVelocity);
		easingCam.velocity.x += dropSpeed * dropAmpltCam * Math.sin(dropDistanceCycle);
		
		// Apply drop impact on camera if is hitting the ground
		if(prevCamVelocity.y < 0D && vec.y > 0D)
		{
			Vec3 v = easingCam.velocity;
			boolean positive = v.x == 0D ? FMUM.rand.nextBoolean() : v.x > 0D;
			if(positive ^ easingCam.curPos.x > 0D)
			{
				v.x = -v.x;
				positive = !positive;
			}
			
			dropSpeed = vec.y * dropImpactAmpltCam;
			v.x += positive ? dropSpeed : -dropSpeed;
		}
		
		this.camOffAxis.update();
	}
	
	protected void updateRenderPosRot(ItemStack stack, TypeInfo type)
	{
		this.renderPos.set(this.renderCamPos);
		this.renderRot.set(this.actualPlayerRot);
	}
	
	/**
	 * Updates {@link #camVelocity} and {@link #prevCamVelocity}
	 */
	protected static void updateCamVelocity()
	{
		final EntityPlayerSP player = getPlayer();
		prevCamVelocity.set(camVelocity);
		vec.set(player.posX, player.posY + player.getEyeHeight(), player.posZ);
		camVelocity.set(vec);
		camVelocity.sub(prevCamPos);
		prevCamPos.set(vec);
	}
	
	protected static boolean getInvertMouse() { return FMUMClient.settings.invertMouse; }
}
