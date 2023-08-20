package com.fmum.common.module;

import com.fmum.util.Mat4f;

public interface ModuleSlot
{
	boolean isCompatible( Module< ? > module );
	
	int maxCapacity();
	
	default int maxStep() { return 0; }
	
	void scaleGeometryParams( float scale );
	
	void applyTransform( Module< ? > child_module, Mat4f dst );
}
