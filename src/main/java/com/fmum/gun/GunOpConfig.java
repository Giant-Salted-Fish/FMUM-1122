package com.fmum.gun;

import com.fmum.animation.SoundFrame;
import com.google.gson.annotations.Expose;
import gsf.util.animation.IAnimation;

public class GunOpConfig
{
	public static final GunOpConfig DEFAULT = new GunOpConfig();
	
	
	@Expose
	public int tick_count;
	
	@Expose
	public int tick_commit;
	
	@Expose
	public IAnimation animation;
	
	@Expose
	public SoundFrame[] sound_frame;
	
	public GunOpConfig()
	{
		this.tick_count = 30;
		this.tick_commit = 20;
		this.animation = IAnimation.EMPTY;
		this.sound_frame = new SoundFrame[ 0 ];
	}
	
	
}
