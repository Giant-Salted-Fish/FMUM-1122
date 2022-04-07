package com.fmum.common.util;

import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.BiConsumer;

public class MotionTracks<T extends MotionTendency>
{
	public static final String
		EASING = "easing",
		ANIMATION = "animation";
	
	protected final TreeMap<String, T> tracks = new TreeMap<>();
	
	public MotionTracks() { }
	
	/**
	 * Create an instance with two default motion tracks {@link #EASING} and
	 * {@link #ANIMATION}
	 */
	public MotionTracks(T easingTrack, T animationTrack)
	{
		this.pushTrack(EASING, easingTrack);
		this.pushTrack(ANIMATION, animationTrack);
	}
	
	public void update()
	{
		for(Entry<String, T> e : this.tracks.entrySet())
			e.getValue().update();
	}
	
	public void update(BiConsumer<String, T> updater) { this.tracks.forEach(updater); }
	
	public T grab(String identifier) { return this.tracks.get(identifier); }
	
	public T pushTrack(String identifier, T motionTrack) {
		return this.tracks.put(identifier, motionTrack);
	}
	
	public T popTrack(String identifier) { return this.tracks.remove(identifier); }
	
	public void getSmoothedPos(Vec3 dst, float smoother)
	{
		dst.set(0D);
		this.applySmoothedPos(dst, smoother);
	}
	
	public void applySmoothedPos(Vec3 dst, float smoother)
	{
		for(Entry<String, T> e : this.tracks.entrySet())
			e.getValue().applySmoothedPos(dst, smoother);
	}
	
	public double getSmoothedX(float smoother)
	{
		double ret = 0D;
		for(Entry<String, T> e : this.tracks.entrySet())
			ret += e.getValue().getSmoothedX(smoother);
		return ret;
	}
	
	public double getSmoothedY(float smoother)
	{
		double ret = 0D;
		for(Entry<String, T> e : this.tracks.entrySet())
			ret += e.getValue().getSmoothedY(smoother);
		return ret;
	}
	
	public double getSmoothedZ(float smoother)
	{
		double ret = 0D;
		for(Entry<String, T> e : this.tracks.entrySet())
			ret += e.getValue().getSmoothedZ(smoother);
		return ret;
	}
}
