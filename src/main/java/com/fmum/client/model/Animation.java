package com.fmum.client.model;

import com.fmum.common.util.ArmTendency;
import com.fmum.common.util.CoordSystem;
import com.fmum.common.util.MotionTendency;
import com.fmum.common.util.MotionTracks;

public class Animation implements Comparable<Animation>
{
	public static final Animation INSTANCE = new Animation();
	
	public void launch() { }
	
	/**
	 * Tick current animation
	 * 
	 * @return {@code true} if this animation has complete
	 */
	public boolean tick() { return false; }
	
	public void onCamUpdate(MotionTracks<? extends MotionTendency> cam) { cam.update(); }
	
	public void onPosRotUpdate(
		MotionTracks<? extends MotionTendency> pos,
		MotionTracks<? extends MotionTendency> rot
	) {
		pos.update();
		rot.update();
	}
	
	public void onArmUpdate(ArmTendency left, ArmTendency right)
	{
		left.update();
		right.update();
	}
	
	public void apply(CoordSystem sys, float smoother) { }
	
	public double getKeyTime() { return 0D; }
	
	@Override
	public int compareTo(Animation a)
	{
		double t0 = this.getKeyTime();
		double t1 = a.getKeyTime();
		return t0 > t1 ? 1 : t0 < t1 ? -1 : 0;
	}
}
