package com.fmum;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.LangKey;

/**
 * Client-side configuration.
 */
//@SideOnly( Side.CLIENT )  // This will crash the load.
@LangKey( "config.fmum.client" )
@Config( modid = FMUM.MODID, category = "client" )
public final class ModConfigClient
{
	@Comment(
		"The default mouse helper used in FMUM may conflict with the mouse "
			+ "helper used in Flan's Mod. This could cause bugs when you enter a "
			+ "vehicle in Flan's Mod. You can try to fix this issue by enabling "
			+ "this setting."
	)
	@LangKey( "config.fmum.client.use_flan_compatible_mousehelper" )
	public static boolean use_flan_compatible_mousehelper = false;
	
	
	private ModConfigClient() { }
}
