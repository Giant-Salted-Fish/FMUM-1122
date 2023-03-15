package com.mcwb.common.module;

import com.mcwb.util.Mat4f;

/**
 * Describes a slot that can install {@link IModule} in it
 * 
 * @author Giant_Salted_Fish
 */
public interface IModuleSlot
{
	public boolean isAllowed( IModule< ? > module );
	
	/**
	 * @return Max number of modules that can be installed into this slot
	 */
	public int capacity();
	
	public default int maxStep() { return 0; }
	
	public void scale( float scale );
	
	public void applyTransform( IModule< ? > installed, Mat4f dst );
}
