package com.fmum.common.util;

public interface Animation extends Comparable<Animation>
{
	/**
	 * Notify this animation to prepare for the launch
	 */
	default public void launch() { }
	
	/**
	 * Tick current animation. Mainly used to switch to non-movement animation when this animation
	 * is finished.
	 * 
	 * @return {@code true} if this animation has complete
	 */
	default public boolean tick(double progress) { return false; }
	
	/**
	 * Set smoothed animation position into destination vector
	 * 
	 * @param dest Vector to contain result value
	 * @param smoothedProgress Progress of the current animation taken in count of partial tick time
	 */
	default public void getSmoothedPos(Vec3 dest, double smoothedProgress) { dest.set(0D); }
	
	/**
	 * Get key frame time of this animation. For animation nodes.
	 * 
	 * @return Key frame time
	 */
	default public double getTime() { return 0D; }
	
	@Override
	default int compareTo(Animation a)
	{
		double t0 = this.getTime();
		double t1 = a.getTime();
		return t0 > t1 ? 1 : t0 < t1 ? -1 : 0;
	}
}
