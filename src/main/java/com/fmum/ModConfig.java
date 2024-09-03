package com.fmum;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.LangKey;
import net.minecraftforge.common.config.Config.RangeDouble;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.Config.RequiresWorldRestart;

@LangKey( "config.fmum.common" )
@Config( modid = FMUM.MODID, category = "common" )
public final class ModConfig
{
	@Comment( "The max number of nested layers allowed for modification." )
	@LangKey( "config.fmum.common.max_module_depth" )
	@RangeInt( min = 1, max = 255 )
	@RequiresWorldRestart
	public static int max_module_depth = 8;
	
	@Comment( "The max number of modules that can be installed into a single slot" )
	@LangKey( "config.fmum.common.max_slot_capacity" )
	@RangeInt( min = 1, max = 254 )
	@RequiresWorldRestart
	public static int max_slot_capacity = 5;
	
	@Comment( "This decides the max angle that you can turn your head around when you hold ALT." )
	@LangKey( "config.fmum.common.free_view_limit" )
	@RangeDouble( min = 0.0D, max = 180.0D )
	@RequiresWorldRestart
	public static float free_view_limit = 130.0F;
	
	@Comment( "This decides the frequency of the camera shake when player falls from sky." )
	@LangKey( "config.fmum.common.camera_drop_cycle" )
	@RequiresWorldRestart
	public static float camera_drop_cycle = 1.0F;
	
	@Comment( "This decides the amplitude of the camera shake when player falls from sky." )
	@LangKey( "config.fmum.common.camera_drop_amplitude" )
	@RequiresWorldRestart
	public static float camera_drop_amplitude = 1.0F;
	
	@Comment( "This decides the amplitude of the camera shake when player impacts on the ground." )
	@LangKey( "config.fmum.common.camera_drop_impact" )
	@RequiresWorldRestart
	public static float camera_drop_impact = 1.0F;
	
	
	private ModConfig() { }
}
