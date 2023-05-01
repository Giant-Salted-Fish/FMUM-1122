package com.fmum.client;

import com.fmum.common.FMUM;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.LangKey;

/**
 * Client only configurations. Unfortunately that currently we have no way to prevent this from
 * loading on physical server side.
 * 
 * @author Giant_Salted_Fish
 */
//@SideOnly( Side.CLIENT ) // Commented as it will crash on load.
@LangKey( "fmum.config.client" )
@Config( modid = FMUM.MODID, category = "client" )
public final class ModConfigClient
{
//	@LangKey( "fmum.config.client.skip_case_when_possible" )
//	@Comment(
//		"Whether to skip ammo case when loading ammo. You can still "
//		+ "load case into mag by pressing \"z\" + pop ammo key."
//	)
//	public static boolean skipCase = true;
}
