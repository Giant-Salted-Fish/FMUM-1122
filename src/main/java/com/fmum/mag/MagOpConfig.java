package com.fmum.mag;

import com.fmum.animation.SoundFrame;
import com.google.gson.annotations.Expose;

public class MagOpConfig
{
	@Expose
	public int tick_count;
	
	@Expose
	public int tick_commit;
	
	@Expose
	public SoundFrame[] sound_frame;
	
	public MagOpConfig()
	{
		this.tick_count = 10;
		this.tick_commit = 7;
		this.sound_frame = new SoundFrame[ 0 ];
	}
	
	public MagOpConfig( int tick_count, int tick_commit, SoundFrame... sound_frame )
	{
		this.tick_count = tick_count;
		this.tick_commit = tick_commit;
		this.sound_frame = sound_frame;
	}
}
