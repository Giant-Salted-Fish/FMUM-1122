package com.fmum.gunpart;

import com.fmum.item.ItemCategory;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import gsf.util.math.Quat4f;
import gsf.util.math.Vec3f;
import gsf.util.render.IPose;

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
	public Vec3f step = Vec3f.ORIGIN;
	
	@Expose
	public short max_step = 0;
	
	@Expose
	public Quat4f rot = Quat4f.IDENTITY;
	
	public IPose getPose( int step )  // TODO: Better param?
	{
		return new IPose() {
			@Override
			public void getPos( Vec3f dst )
			{
				dst.set( RailSlot.this.step );
				dst.scaleAdd( step, RailSlot.this.origin );
			}
			
			@Override
			public void getRot( Quat4f dst ) {
				dst.set( RailSlot.this.rot );
			}
		};
	}
}
