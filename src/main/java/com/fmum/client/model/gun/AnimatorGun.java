package com.fmum.client.model.gun;

import com.fmum.client.FMUMClient;
import com.fmum.client.KeyManager.Key;
import com.fmum.client.model.AnimatorCamControl;
import com.fmum.common.FMUM;
import com.fmum.common.gun.TagGun;
import com.fmum.common.gun.TypeGunPart;
import com.fmum.common.module.InfoModule;
import com.fmum.common.module.TypeModular;
import com.fmum.common.type.TypeInfo;
import com.fmum.common.util.Animation;
import com.fmum.common.util.ArmTendency;
import com.fmum.common.util.MotionTendencyBased;
import com.fmum.common.util.CoordSystem;
import com.fmum.common.util.MotionTendency;
import com.fmum.common.util.MotionTracks;
import com.fmum.common.util.Vec3;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;

public class AnimatorGun extends AnimatorCamControl
{
	public static final AnimatorGun INSTANCE = new AnimatorGun();
	
	protected static final Vec3 prevPlayerRot = new Vec3();
	
	protected static final InfoModule
		leftHandGrabbing = new InfoModule(),
		rightHandGrabbing = new InfoModule();
	
	protected static double breathCycle = 0D;
	
	protected static double walkDistanceCycle = 0D;
	
	public final MotionTracks<MotionTendency>
		pos = new MotionTracks<>(
			new MotionTendencyBased(0.4D, 0.125D, 0.25D),
			new MotionTendencyBased(0.4D, 0.125D, 0.25D)
		),
		rot = new MotionTracks<>(
			new MotionTendencyBased(0.4D, 4.25D, 1D),
			new MotionTendencyBased(0.4D, 4.25D, 1D)
		);
	
	public final ArmTendency
		leftArmPos = new ArmTendency(),
		rightArmPos = new ArmTendency();
	
	/**
	 * Animation that is currently playing
	 */
	public AnimationGun animation = AnimationGun.NONE;
	
	public AnimatorGun()
	{
		// Setup default shoulder position for arms
		vec.set(1.15D / 16D, -4D / 16D, -5D / 16D);
		this.leftArmPos.setShoulderTarPos(vec);
		vec.set(-1.85D / 16D, -4D / 16D, 3D / 16D);
		this.rightArmPos.setShoulderTarPos(vec);
	}
	
	@Override
	public void launchAnimation(Animation animation)
	{
		this.animation = ((AnimationGun)animation);
		animation.launch();
	}
	
	@Override
	public void applyTransform(CoordSystem sys, float smoother)
	{
		this.pos.getSmoothedPos(vec, smoother);
		sys.trans(vec);
		this.rot.getSmoothedPos(vec, smoother);
		sys.rot(vec);
		sys.submitRot();
		
		// Do not forget to apply the playing animation
		this.animation.applyGunTransform(sys, FMUMClient.operating.getSmoothedProgress(smoother));
	}
	
	@Override
	protected void doItemTick(ItemStack stack, TypeInfo type)
	{
		if(this.animation.tick(FMUMClient.operating.getProgress()))
			this.animation = AnimationGun.NONE;
		
		// Prepare values
		final EntityPlayerSP player = getPlayer();
		final boolean aiming = Key.AIM_HOLD.down();
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
		updateCamVelocity();
		vec.set(camVelocity);
		double dropSpeed = Math.min(0D, vec.y);
		double moveSpeed = player.onGround ? Math.sqrt(vec.x * vec.x + vec.z * vec.z) : 0D;
		
		dropDistanceCycle += dropSpeed * model.dropCycle;
		walkDistanceCycle += moveSpeed * (crouching ? model.crouchWalkCycle : model.walkCycle);
		
		// Get acceleration and check drop impact(hit ground)
		vec.sub(prevCamVelocity);
		double verticalDropAcc = vec.y;
		final boolean dropImpact = prevCamVelocity.y < 0D && verticalDropAcc > 0D;
		vec.y *= model.camDropAccMult;
		
		// Transfer acceleration into player's eye coordinate system
		sys.setDefault();
		sys.globalRot(this.renderCamRot.y + 90D, CoordSystem.Y);
		sys.globalRot(this.renderCamRot.z, CoordSystem.Z);
		sys.apply(vec, vec);
		
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
		
		// Get acceleration relative to render coordinate system(shoulder system)
		vec.set(camVelocity);
		vec.sub(prevCamVelocity);
		sys.setDefault();
		sys.globalRot(this.renderRot.y + 90D, CoordSystem.Y);
		sys.globalRot(this.renderRot.z, CoordSystem.Z);
		sys.apply(vec, vec);
		
		v = model.smoothMotion_P;
		easingPos.velocity.trans(vec.x * v.x, vec.y * v.y, vec.z * v.z);
		v = model.smoothMotion_R;
		easingRot.velocity.trans(vec.z * v.x, vec.z * v.y, vec.y * v.z);

		/// Walk/sprint bobbing animation ///
		// camera walk bobbing
		cos = moveSpeed * Math.cos(walkDistanceCycle);
		v = sprinting ? model.sprintAmpltCam : model.walkAmpltCam;
		easingCam.velocity.trans(cos * v.x, cos * v.y, Math.abs(cos) * v.z);
		
		// Gun walk/sprint animation
		vec.set(camVelocity);
		sys.apply(vec, vec);
		cos = moveSpeed * Math.cos(walkDistanceCycle + Math.PI * 0.5D);
		sin = Math.abs(cos) - 0.5D * moveSpeed;
		v = sprinting ? model.sprintAmpltGun_P : model.walkAmpltGun_P;
		easingPos.velocity.trans(cos * v.x, sin * v.y, cos * v.z);
		v = sprinting ? model.sprintAmpltCompensationGun_P : model.walkAmpltCompensationGun_P;
		easingPos.velocity.trans(sin * vec.x * v.x, sin * vec.y * v.y, sin * vec.z * v.z);
		v = sprinting ? model.sprintAmpltGun_R : model.walkAmpltGun_R;
		easingRot.velocity.trans(cos * v.x, cos * v.y, sin * v.z);
		
		/// View smooth ///
		double vRotYaw = player.rotationYaw - prevPlayerRot.y;
		double vRotPitch = player.rotationPitch - prevPlayerRot.z;
		
		v = model.smoothViewRot_P;
		easingPos.velocity.trans(0D, vRotPitch * v.y, vRotYaw * v.z);
		v = model.smoothViewRot_R;
		easingRot.velocity.trans(vRotYaw * v.x, vRotYaw * v.y, vRotPitch * v.z);
		
		// Set target position
		vec.set(
			aiming
			? model.aimMoveOffset
			: (
				sprinting
				? model.sprintMoveOffset
				: crouching ? model.crouchMoveOffset : model.holdMoveOffset
			)
		);
		vec.scale(moveSpeed);
		easingPos.tarPos.set(vec);
		vec.set(
			aiming
			? model.aimShoulderOffset
			: (
				sprinting
				? model.sprintShoulderOffset
				: crouching ? model.crouchShoulderOffset : model.holdShoulderOffset
			)
		);
		vec.scale(this.actualPlayerRot.z);
		easingPos.tarPos.trans(vec);
		easingPos.tarPos.trans(
			aiming
			? model.aimPos
			: (
				sprinting
				? model.sprintPos
				: crouching ? model.crouchPos : model.holdPos
			)
		);
		
		easingRot.tarPos.set(
			aiming
			? model.aimRot
			: (
				sprinting
				? model.sprintRot
				: crouching ? model.crouchRot : model.holdRot
			)
		);
		
		// Update camera, position and rotation
		this.animation.onCamUpdate(this.camOffAxis);
		this.animation.onGunUpdate(this.pos, this.rot);
		
		/// Update arm/hand position ///
		TypeModular gun = (TypeModular)type;
		leftHandGrabbing.type
			= rightHandGrabbing.type
			= gun;
		gun.stream(
			TagGun.getTag(stack),
			0D, 0D, 0D,
			0D,
			(tag, typ, x, y, z, sin0, cos0, rotX) -> {
				TypeGunPart part = (TypeGunPart)typ;
				final boolean leftGrab = part.leftHandPriority
					>= ((TypeGunPart)leftHandGrabbing.type).leftHandPriority;
				final boolean rightGrab = part.rightHandPriority
					>= ((TypeGunPart)rightHandGrabbing.type).rightHandPriority;
				if(leftGrab || rightGrab)
				{
					final InfoModule info = leftGrab ? leftHandGrabbing : rightHandGrabbing;
					info.type = typ;
					info.x = x;
					info.y = y;
					info.z = z;
					info.sin = sin0;
					info.cos = cos0;
					info.rotX = rotX;
				}
				return false;
			}
		);
		
		sys.setDefault();
		this.applyTransform(sys, 1F);
		((ModelGrable)leftHandGrabbing.type.model).updateLeftHandTarPos(
			this,
			sys,
			leftHandGrabbing,
			this.leftArmPos
		);
		((ModelGrable)rightHandGrabbing.type.model).updateRightHandTarPos(
			this,
			sys,
			rightHandGrabbing,
			this.rightArmPos
		);
		
		this.animation.onArmUpdate(this.leftArmPos, this.rightArmPos);
		
		// Update last tick values
		prevPlayerRot.set(0D, player.rotationYaw, player.rotationPitch);
	}
}
