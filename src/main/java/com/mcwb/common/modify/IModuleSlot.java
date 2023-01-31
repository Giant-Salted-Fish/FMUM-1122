package com.mcwb.common.modify;

import com.mcwb.util.Mat4f;

/**
 * Describes a slot that can install {@link IModifiableMeta} in it
 * 
 * @author Giant_Salted_Fish
 */
public interface IModuleSlot
{
	public boolean isAllowed( IContextedModifiable module );
	
	/**
	 * @return Max number of modules that can be installed into this slot
	 */
	public int capacity();
	
	public default int maxStep() { return 0; }
	
//	public default void getStep( String channel, int step, Vec3f dst ) { dst.set( 0F ); }
//	
//	public default int getMaxStep( String channel ) { return 1; }
	
	public void scale( float scale );
	
	public void applyTransform( IContextedModifiable installed, Mat4f dst );
}
