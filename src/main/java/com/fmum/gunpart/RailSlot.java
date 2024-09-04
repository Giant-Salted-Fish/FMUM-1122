package com.fmum.gunpart;

import com.fmum.item.ItemCategory;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import gsf.util.math.Vec3f;

import java.util.function.Predicate;

public class RailSlot
{
	@Expose
	@SerializedName( "allowed" )
	public Predicate< ItemCategory > category_predicate;
	
	@Expose
	public int capacity = 1;
	
	@Expose
	public Vec3f origin = Vec3f.ORIGIN;
	
	@Expose
	public float step_len = 0.0F;
	
	@Expose
	public short max_step = 0;
	
	/**
	 * The rotation around the x-axis in degrees.
	 */
	@Expose
	public float rot_z = 0.0F;
}
