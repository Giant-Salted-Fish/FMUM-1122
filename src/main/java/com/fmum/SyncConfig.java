package com.fmum;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * For those {@link ModConfig} settings that need to be synchronized to client
 * side, or will be used on both side.
 */
public final class SyncConfig
{
	public static int max_module_depth;
	public static int max_slot_capacity;
	
	@SideOnly( Side.CLIENT )
	public static float free_view_limit_squared;
	
	@SideOnly( Side.CLIENT )
	public static float camera_drop_cycle;
	
	@SideOnly( Side.CLIENT )
	public static float camera_drop_amplitude;
	
	@SideOnly( Side.CLIENT )
	public static float camera_drop_impact;
	
	
	private SyncConfig() { }
}
