package com.fmum.module;

import com.fmum.item.ItemCategory;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import gsf.util.math.Vec3f;

import java.util.function.Predicate;

public class FixedSlot
{
	@Expose
	@SerializedName( "allowed" )
	public Predicate< ItemCategory > category_predicate;
	
	@Expose
	public int capacity = 1;
	
	@Expose
	public Vec3f origin = Vec3f.ORIGIN;
}
