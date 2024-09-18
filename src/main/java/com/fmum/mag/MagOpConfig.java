package com.fmum.mag;

import com.google.gson.annotations.Expose;

public class MagOpConfig
{
	@Expose
	public float progressor = 0.1F;
	
	@Expose
	public float effect_time = 0.5F;
	
	public MagOpConfig() { }
	
	public MagOpConfig( float progressor, float effect_time )
	{
		this.progressor = progressor;
		this.effect_time = effect_time;
	}
}
