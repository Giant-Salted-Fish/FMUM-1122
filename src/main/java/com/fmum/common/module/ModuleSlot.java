package com.fmum.common.module;

import com.fmum.common.util.CoordSystem;

/**
 * Interface of a slot that can install {@link MetaModular} on it
 * 
 * @author Giant_Salted_Fish
 */
public interface ModuleSlot
{
	public default ModuleSlot rescale( double s ) { return this; }
	
	public default boolean isAllowed( MetaModular module ) { return false; }
	
	public default int maxCanInstall() { return 1; }
	
	public default int maxPosStep() { return 0; }
	
	public default int maxStep( int channel ) { return 0; }
	
	public default double posStep() { return 0D; }
	
	public default double step( int channel ) { return 0D; }
	
	/**
	 * Apply primary slot transform to the given coordinate system
	 */
	public default void apply( CoordSystem sys ) { }
}
