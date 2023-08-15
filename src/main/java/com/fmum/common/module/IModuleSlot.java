package com.fmum.common.module;

import com.fmum.util.Mat4f;

public interface IModuleSlot
{
	boolean isCompatible( IModule< ? > module );
	
	int maxCapacity();
	
	default int maxStep() { return 0; }
	
	void scaleGeometryParams( float scale );
	
	void applyTransform( IModule< ? > child_module, Mat4f dst );
}
