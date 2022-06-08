package com.fmum.common.module;

import com.fmum.common.util.CoordSystem;

/**
 * Describes a slot that can install {@link Modular}s on it
 * 
 * @author Giant_Salted_Fish
 */
public interface ModuleSlot
{
	public default ModuleSlot rescale( double s ) { return this; }
	
	public default boolean isAllowed( MetaModular module ) { return false; }
	
	public default int maxCanInstall() { return 1; }
	
	public default int maxPosStep() { return 0; }
	
	public default int maxRotStep() { return 0; }
	
	public default double posStepX() { return 0D; }
	
	public default double posStepY() { return 0D; }
	
	public default double posStepZ() { return 0D; }
	
	public default double rotStepX() { return 0D; }
	
	public default double rotStepY() { return 0D; }
	
	public default double rotStepZ() { return 0D; }
	
	public default void applyTransform( CoordSystem sys ) { }
}
