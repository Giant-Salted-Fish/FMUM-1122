package com.fmum.common.util;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.TreeMap;

public class MotionTracks<T extends BasedMotionTendency>
{
	public static final String
		EASING = "easing",
		ANIMATION = "animation";
	
	public final TreeMap<String, Entry<T, Integer>> tracks = new TreeMap<>();
	
	public MotionTracks() { }
	
	public int getOccupied(String identifier)
	{
		Entry<T, Integer> e = this.tracks.get(identifier);
		return e != null ? e.getValue() : -1;
	}
	
//	public T grabEasing() { return this.grab(TRACK_EASING); }
//	
//	public T grabAnimation() { return this.grab(TRACK_ANIMATION); }
	
	public void update()
	{
		for(Entry<String, Entry<T, Integer>> e : this.tracks.entrySet())
			e.getValue().getKey().update();
	}
	
	public T grab(String identifier)
	{
		Entry<T, Integer> e = this.tracks.get(identifier);
		e.setValue(e.getValue() + 1);
		return e.getKey();
	}
	
	public void release(String identifier)
	{
		Entry<T, Integer> e = this.tracks.get(identifier);
		e.setValue(e.getValue() - 1);
	}
	
	/**
	 * Create an instance with two default motion tracks {@link #EASING} and
	 * {@link #ANIMATION}
	 */
	public MotionTracks(T easingTrack, T animationTrack)
	{
		this.pushTrack(EASING, easingTrack);
		this.pushTrack(ANIMATION, animationTrack);
	}
	
	public T pushTrack(String identifier, T motionTrack)
	{
		Entry<T, Integer> e = this.tracks.put(identifier, new SimpleEntry<>(motionTrack, 0));
		return e != null ? e.getKey() : null;
	}
	
	public T popTrack(String identifier)
	{
		Entry<T, Integer> e = this.tracks.remove(identifier);
		return e != null ? e.getKey() : null;
	}
	
	public Entry<T, Integer> push(String identifier, Entry<T, Integer> trackEntry) {
		return this.tracks.put(identifier, trackEntry);
	}
	
	public Entry<T, Integer> pop(String identifier) { return this.tracks.remove(identifier); }
	
	public void getSmoothedPos(Vec3 dest, float smoother)
	{
		dest.set(0D);
		this.applySmoothedPos(dest, smoother);
	}
	
	public void applySmoothedPos(Vec3 dest, float smoother)
	{
		for(Entry<String, Entry<T, Integer>> e : this.tracks.entrySet())
			e.getValue().getKey().applySmoothedPos(dest, smoother);
	}
}
