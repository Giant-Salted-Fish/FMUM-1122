package com.fmum.client.gun.model;

import com.fmum.common.util.Animation;
import com.fmum.common.util.ArmTendency;
import com.fmum.common.util.CoordSystem;
import com.fmum.common.util.MotionTendency;
import com.fmum.common.util.MotionTracks;

public interface AnimationGun extends Animation
{
	public static final AnimationGun NONE = new AnimationGun() { };
	
	default public void onCamUpdate(MotionTracks<? extends MotionTendency> cam) { cam.update(); }
	
	default public void onGunUpdate(
		MotionTracks<? extends MotionTendency> pos,
		MotionTracks<? extends MotionTendency> rot
	) {
		pos.update();
		rot.update();
	}
	
	default public void onArmUpdate(ArmTendency left, ArmTendency right)
	{
		left.update();
		right.update();
	}
	
	default public void applyGunTransform(CoordSystem sys, double smoothedProgress) { }
}
