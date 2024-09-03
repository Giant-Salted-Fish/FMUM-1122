package com.fmum.gunpart;

import com.fmum.module.FixedSlot;
import com.google.gson.annotations.Expose;

public class RailSlot extends FixedSlot
{
	/**
	 * The rotation around the x-axis in degrees.
	 */
	@Expose
	public float rot_z = 0.0F;
	
	@Expose
	public float step_len = 0.0F;
	
	@Expose
	public short max_step = 0;
}
