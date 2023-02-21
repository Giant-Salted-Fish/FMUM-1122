package com.mcwb.client;

import com.mcwb.common.MCWB;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.LangKey;

//@SideOnly( Side.CLIENT ) // Commented as it will crash on load
@LangKey( "mcwb.config.client" )
@Config( modid = MCWB.ID, category = "client" )
public final class ModConfigClient
{
//	@LangKey( "mcwb.config.client.skip_case_when_possible" )
//	@Comment(
//		"Whether to skip ammo case when loading ammo. You can still "
//		+ "load case into mag by pressing \"z\" + pop ammo key."
//	)
//	public static boolean skipCase = true;
}
