package com.fmum.common.gun;

import com.fmum.common.module.ModuleCategory;
import com.fmum.common.player.IOperationController;

/**
 * Used by TODO to select proper {@link IOperationController} under certain conditions.
 * 
 * @author Giant_Salted_Fish
 */
public interface IControllerRanker
{
	int getRank( String key, boolean value );
	
	int getRank( String key, ModuleCategory category );
}
