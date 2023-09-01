package com.fmum.common.module;

import com.fmum.util.CategoryDomain;
import com.fmum.util.Mat4f;
import com.fmum.util.Vec3f;
import com.google.gson.annotations.SerializedName;

/**
 * A simple implementation that represents a slot that provides a fixed attach point.
 */
public class PointSlot implements IModuleSlot
{
	@SerializedName( value = "category_domain", alternate = "allowed_modules" )
	protected CategoryDomain category_domain = CategoryDomain.DEFAULT;
	
	protected int max_capacity = 1;
	
	protected Vec3f origin = Vec3f.ORIGIN;
	
	@Override
	public boolean isCompatible( IModule< ? > module ) {
		return this.category_domain.isCompatible( module.category() );
	}
	
	@Override
	public int maxCapacity() {
		return this.max_capacity;
	}
	
	@Override
	public void scaleGeometryParams( float scale ) {
		this.origin.scale( scale );
	}
	
	@Override
	public void applyTransform( IModule< ? > child_module, Mat4f dst ) {
		dst.translate( this.origin );
	}
}
