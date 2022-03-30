package com.fmum.client.model.gun;

import com.fmum.client.FMUMClient;
import com.fmum.client.model.Animation;
import com.fmum.client.model.CamControlAnimator;
import com.fmum.common.FMUM;
import com.fmum.common.type.TypeInfo;
import com.fmum.common.util.BasedMotionTendency;
import com.fmum.common.util.CoordSystem;
import com.fmum.common.util.MotionTendency;
import com.fmum.common.util.MotionTracks;
import com.fmum.common.util.Vec3;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;

public class GunAnimator extends CamControlAnimator
{
	public static final GunAnimator INSTANCE = new GunAnimator();
	
	protected static final Vec3 prevPlayerRot = new Vec3();
	
	protected static double breathCycle = 0D;
	
	protected static double walkDistanceCycle = 0D;
	
	public final MotionTracks<BasedMotionTendency>
		pos = new MotionTracks<>(
			new BasedMotionTendency(0.4D, 0.125D, 0.25D),
			new BasedMotionTendency(0.4D, 0.125D, 0.25D)
		),
		rot = new MotionTracks<>(
			new BasedMotionTendency(0.4D, 4.25D, 1D),
			new BasedMotionTendency(0.4D, 4.25D, 1D)
		);
	
	/**
	 * Animation that is currently playing
	 */
	public Animation animation = Animation.INSTANCE;
	
	@Override
	public void doItemTick(ItemStack stack, TypeInfo type)
	{
		if(this.animation.tick())
			this.animation = Animation.INSTANCE;
		
		// Prepare values
		final EntityPlayerSP player = getPlayer();
		final boolean crouching = player.isSneaking();
		final boolean sprinting = player.isSprinting();
		final ModelGun model = (ModelGun)type.model;
		final MotionTendency easingCam = this.camOffAxis.grab(MotionTracks.EASING);
		final MotionTendency easingPos = this.pos.grab(MotionTracks.EASING);
		final MotionTendency easingRot = this.rot.grab(MotionTracks.EASING);
		Vec3 v;
		
		/// Breath cycle ///
		// Impact on camera
		double airLost = getAirLost();
		breathCycle += model.breathCycleBase + model.breathCycleIncr * airLost;
		double sin = Math.sin(breathCycle * 2D);
		double cos = Math.cos(breathCycle);
		vec.set(model.breathAmpltCamIncr);
		vec.scale(airLost);
		vec.trans(model.breathAmpltCamBase);
		easingCam.velocity.trans(
			cos * vec.x,
			cos * vec.y,
			sin * vec.z
		);
		
		// Impact on gun
		vec.set(model.breathAmpltGunIncr_P);
		vec.scale(airLost);
		vec.trans(model.breathAmpltGunBase_P);
		easingPos.velocity.trans(
			cos * vec.x,
			sin * vec.y,
			cos * vec.z
		);
		
		vec.set(model.breathAmpltGunIncr_R);
		vec.scale(airLost);
		vec.trans(model.breathAmpltGunBase_R);
		easingRot.velocity.trans(
			cos * vec.x,
			cos * vec.y,
			sin * vec.z
		);
		
		/// Motion smooth ///
		// Get motion of the player
		updatePlayerVelocity();
		vec.set(playerVelocity);
		double dropSpeed = Math.min(0D, vec.y);
		double moveSpeed = player.onGround ? Math.sqrt(vec.x * vec.x + vec.z * vec.z) : 0D;
		
		dropDistanceCycle += dropSpeed * model.dropCycle;
		walkDistanceCycle += moveSpeed * (crouching ? model.crouchWalkCycle : model.walkCycle);
		
		// Get acceleration and check drop impact(hit ground)
		vec.sub(prevPlayerVelocity);
		double verticalDropAcc = vec.y;
		final boolean dropImpact = prevPlayerVelocity.y < 0D && verticalDropAcc > 0D;
		if(dropImpact)
			vec.y *= model.dropImpactAccCamMult;
		
		// Transfer acceleration into player's eye coordinate system
		sys.setDefault();
		sys.globalRot(this.renderEyeRot.y + 90D, CoordSystem.Y);
		sys.apply(vec, vec);
		FMUMClient.toggleManualTell(() -> vec.toString() + ", drop speed: " + dropSpeed);
		
		v = model.motionAmpltCam;
		easingCam.velocity.x += vec.z * v.z
			+ dropSpeed * model.dropAmpltCam * Math.sin(dropDistanceCycle);
		easingCam.velocity.z += vec.x * v.x + vec.y * v.y;
		
		// Apply drop impact on camera if has
		if(dropImpact)
		{
			v = easingCam.velocity;
			boolean positive = v.x == 0D ? FMUM.rand.nextBoolean() : v.x > 0D;
			if(positive ^ easingCam.curPos.x > 0D)
			{
				v.x = -v.x;
				positive = !positive;
			}
			
			double impact = verticalDropAcc * model.dropImpactAmpltCam;
			v.x += positive ? impact : -impact;
		}
		
		// Setup camera walk bobbing
		cos = moveSpeed * Math.cos(walkDistanceCycle);
		v = sprinting ? model.sprintAmltCam : model.walkAmpltCam;
//		easingCam.velocity.trans(cos * v.x, cos * v.y, Math.abs(cos) * v.z);
		
		// Get acceleration relative to render coordinate system(shoulder system)
//		vec.set(playerVelocity);
//		vec.sub(prevPlayerVelocity);
//		sys.setDefault();
//		sys.globalRot(amount, along);
		
		/// View smooth ///
		double vRotYaw = player.rotationYaw - prevPlayerRot.y;
		double vRotPitch = player.rotationPitch - prevPlayerRot.z;
		
		v = model.smoothViewRot_P;
		easingPos.velocity.trans(
			0D + 0D,
			0D + vRotPitch * v.y,
			0D + vRotYaw * v.z
		);
		v = model.smoothViewRot_R;
		easingRot.velocity.trans(
			vRotYaw * v.x,
			vRotYaw * v.y,
			vRotPitch * v.z
		);
		
		// Set target position
		easingPos.tarPos.set(model.holdPos);
		easingRot.tarPos.set(model.holdRot);
		
		// Update position and rotation
		this.rot.update();
		this.pos.update();
		this.camOffAxis.update();
		
		// Update last tick values
		prevPlayerRot.set(0D, player.rotationYaw, player.rotationPitch);
	}
	
	@Override
	public void applyTransform(CoordSystem sys, float smoother)
	{
		this.pos.getSmoothedPos(vec, smoother);
		sys.trans(vec);
		this.rot.getSmoothedPos(vec, smoother);
		sys.rot(vec);
		sys.submitRot();
	}
}
