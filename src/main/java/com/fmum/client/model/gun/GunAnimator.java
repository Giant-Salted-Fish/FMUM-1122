package com.fmum.client.model.gun;

import com.fmum.client.model.Animation;
import com.fmum.client.model.ShoulderBasedAnimator;
import com.fmum.common.type.TypeInfo;
import com.fmum.common.util.BasedMotionTendency;
import com.fmum.common.util.CoordSystem;
import com.fmum.common.util.MotionTendency;
import com.fmum.common.util.MotionTracks;
import com.fmum.common.util.Vec3;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;

public class GunAnimator extends ShoulderBasedAnimator
{
	public static final GunAnimator INSTANCE = new GunAnimator();
	
	protected static final Vec3 prevPlayerRot = new Vec3();
	
	public final MotionTracks<BasedMotionTendency>
		pos = new MotionTracks<>(
			new BasedMotionTendency(0.4F, 0.125F, 0.25F),
			new BasedMotionTendency(0.4F, 0.125F, 0.25F)
		),
		rot = new MotionTracks<>(
			new BasedMotionTendency(0.4F, 4.25F, 1F),
			new BasedMotionTendency(0.4F, 4.25F, 1F)
		);
	
	/**
	 * Animation that is currently playing
	 */
	public Animation animation = Animation.INSTANCE;
	
	@Override
	public void itemTick(ItemStack stack, TypeInfo type)
	{
		if(this.animation.tick())
			this.animation = Animation.INSTANCE;
		
		// Prepare values
		final EntityPlayerSP player = getPlayer();
		final ModelGun model = (ModelGun)type.model;
		final MotionTendency easingRot = this.rot.grab(MotionTracks.EASING);
		final MotionTendency easingPos = this.pos.grab(MotionTracks.ANIMATION);
		
		// TODO: breath cycle
		
		// View smooth
		double vRotYaw = player.rotationYaw - prevPlayerRot.y;
		double vRotPitch = player.rotationPitch - prevPlayerRot.z;
		double[] da = model.viewRotInertia;
		
		easingPos.velocity.trans(
			0D + 0D,
			0D + vRotPitch * da[1],
			0D + vRotYaw * da[2]
		);
		easingRot.velocity.trans(
			vRotYaw * da[3],
			vRotYaw * da[4],
			vRotPitch * da[5]
		);
		
		// Set target position
		easingPos.tarPos.set(model.holdPos);
		easingRot.tarPos.set(model.holdRot);
		
		// Update position and rotation
		this.rot.update();
		this.pos.update();
		
		// Release tendency tracks
		this.rot.release(MotionTracks.EASING);
		this.pos.release(MotionTracks.ANIMATION);
		
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
	
	@Override
	protected void updateShoulderPosRot(ItemStack stack, TypeInfo type)
	{
		sys.setDefault();
		sys.globalTrans(this.renderEyePos);
		sys.globalRot(-this.renderEyeRot.y - 90D, CoordSystem.Y);
		sys.trans(0.05D, CoordSystem.NORM_X);
		sys.get(this.shoulderPos, CoordSystem.OFFSET);
		this.shoulderPos.y -= 0.05D;
		
		this.shoulderRot.set(this.renderEyeRot);
	}
}
