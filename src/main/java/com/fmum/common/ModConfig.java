package com.fmum.common;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.LangKey;
import net.minecraftforge.common.config.Config.RangeDouble;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.Config.RequiresWorldRestart;

/**
 * Configurations that will be loaded on both client and server side.
 * 
 * @author Giant_Salted_Fish
 */
@LangKey( "fmum.config.common" )
@Config( modid = FMUM.MODID, category = "common" )
public final class ModConfig
{
	@LangKey( "fmum.config.common.max_modify_layers" )
	@Comment( "This decides the max number of layers you can have when you modify your weapon." )
	@RangeInt( min = 1, max = 255 )
	@RequiresWorldRestart
	public static int maxModifyLayers = 8;
	
	@LangKey( "fmum.config.common.max_slot_capacity" )
	@Comment( "This decides the max number of modules that you can installed in a single slot." )
	@RangeInt( min = 1, max = 254 )
	@RequiresWorldRestart
	public static int maxSlotCapacity = 5;
	
	@LangKey( "fmum.config.common.free_view_limit" )
	@Comment( "This decides the max angle that you can turn your head around when you hold ALT." )
	@RangeDouble( min = 0D, max = 180D )
	@RequiresWorldRestart
	public static float freeViewLimit = 130F;
	
	@LangKey( "fmum.config.common.cam_drop_cycle" )
	@Comment( "This decides the frequency of the camera shake when player falls from sky." )
	@RequiresWorldRestart
	public static float camDropCycle = 1F;
	
	@LangKey( "fmum.config.common.cam_drop_ampl")
	@Comment( "This decides the amplitude of the camera shake when player falls from sky." )
	@RequiresWorldRestart
	public static float camDropAmpl = 1F;
	
	@LangKey( "fmum.config.common.cam_drop_impact" )
	@Comment( "This decides the amplitude of the camera shake when player impacts on the ground." )
	@RequiresWorldRestart
	public static float camDropImpact = 1F;
	
	@LangKey( "fmum.config.common.shoot_request_timeout_ticks" )
	@Comment( "How long the server will wait for next client shoot request to continue fire." )
	@RangeInt( min = 1 )
	public static int shootRequestTimeoutTicks = 5;
}
