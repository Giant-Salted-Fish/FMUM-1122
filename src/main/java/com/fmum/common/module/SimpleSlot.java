package com.fmum.common.module;

import com.fmum.util.Mat4f;
import com.fmum.util.Vec3f;
import com.google.gson.annotations.SerializedName;

public class SimpleSlot implements IModuleSlot
{
	/**
	 * Decides what can be installed and what can not.
	 */
	@SerializedName( value = "moduleFilter", alternate = "allowed" )
	protected ModuleFilter moduleFilter = ModuleFilter.DEFAULT;
	
	/**
	 * Maximum number of modules can be installed into this slot.
	 */
	@SerializedName( value = "capacity", alternate = "maxCanInstall" )
	public byte capacity = 1;
	
	/**
	 * Coordinate of the root of the slot.
	 */
	protected Vec3f origin = Vec3f.ORIGIN;
	
	@Override
	public boolean isAllowed( IModule< ? > module ) {
		return this.moduleFilter.isAllowed( module.category() );
	}
	
	@Override
	public int capacity() { return this.capacity; }
	
	@Override
	public void scale( float scale ) { this.origin.scale( scale ); }
	
	@Override
	public void applyTransform( IModule< ? > installed, Mat4f dst ) {
		dst.translate( this.origin );
	}
}
