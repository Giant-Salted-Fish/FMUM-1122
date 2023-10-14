package gsf.fmum.common.module;

import gsf.fmum.util.Mat4f;

public interface IModuleSlot
{
	boolean isCompatibleWith( IModule module );
	
	int capacity();
	
	default int maxStep() { return 0; }
	
	void scaleParam( float scale );
	
	void applyTransform( IModule child_module, Mat4f dst );
}
