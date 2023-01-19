package com.mcwb.util;

public interface Animation extends Comparable< Animation >
{
	/**
	 * Notify this animation to prepare for the launch
	 */
	public default void launch() { }
	
	/**
	 * Tick current animation. Mainly used to switch to non-movement animation when this animation
	 * is finished.
	 * 
	 * @return {@code true} if this animation has complete
	 */
	public default boolean tick( float progress ) { return false; }
	
	/**
	 * Set smoothed animation position into destination vector
	 * 
	 * @param dst Vector to contain result value
	 * @param smoothedProgress Progress of the current animation taken in count of partial tick time
	 */
	public default void getSmoothedPos( Vec3f dst, float smoothedProgress ) { dst.set( 0F ); }
	
	/**
	 * Get key frame time of this animation. For animation nodes.
	 * 
	 * @return Key frame time
	 */
	public default float getTime() { return 0F; }
	
	@Override
	public default int compareTo( Animation a )
	{
		float t0 = this.getTime();
		float t1 = a.getTime();
		return t0 > t1 ? 1 : t0 < t1 ? -1 : 0;
	}
}
