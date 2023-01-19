package com.mcwb.util;

import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.BiConsumer;

public class MotionTracks< T extends MotionTendency >
{
	public static final String
		EASING = "easing",
		ANIMATION = "animation";
	
	protected final TreeMap< String, T > tracks = new TreeMap<>();
	
	public MotionTracks() { }
	
	/**
	 * Create an instance with two default motion tracks {@link #EASING} and
	 * {@link #ANIMATION}
	 */
	public MotionTracks( T easingTrack, T animationTrack )
	{
		this.pushTrack( EASING, easingTrack );
		this.pushTrack( ANIMATION, animationTrack );
	}
	
	public void update()
	{
		for( Entry< String, T > e : this.tracks.entrySet() )
			e.getValue().update();
	}
	
	public void update( BiConsumer< String, T > updater ) { this.tracks.forEach( updater ); }
	
	public T grab( String identifier ) { return this.tracks.get( identifier ); }
	
	public T pushTrack( String identifier, T motionTrack ) {
		return this.tracks.put( identifier, motionTrack );
	}
	
	public T popTrack( String identifier ) { return this.tracks.remove( identifier ); }
	
	public void getSmoothedPos( Vec3f dst, float smoother )
	{
		dst.set( 0F );
		this.applySmoothedPos( dst, smoother );
	}
	
	public void applySmoothedPos( Vec3f dst, float smoother )
	{
		for( Entry< String, T > e : this.tracks.entrySet() )
			e.getValue().applyPos( dst, smoother );
	}
	
	public float getSmoothedX( float smoother )
	{
		float ret = 0F;
		for( Entry< String, T > e : this.tracks.entrySet() )
			ret += e.getValue().getX( smoother );
		return ret;
	}
	
	public float getSmoothedY( float smoother )
	{
		float ret = 0F;
		for(Entry< String, T > e : this.tracks.entrySet() )
			ret += e.getValue().getY( smoother );
		return ret;
	}
	
	public float getSmoothedZ( float smoother )
	{
		float ret = 0F;
		for( Entry< String, T > e : this.tracks.entrySet() )
			ret += e.getValue().getZ( smoother );
		return ret;
	}
}
