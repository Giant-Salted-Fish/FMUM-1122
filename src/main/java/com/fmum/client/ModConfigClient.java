package com.fmum.client;

import com.fmum.common.FMUM;
import net.minecraft.init.Items;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.LangKey;
import net.minecraftforge.common.config.Config.RequiresMcRestart;

/**
 * Client only configurations.
 */
//@SideOnly( Side.CLIENT ) // Commented as it will crash the load.
@LangKey( "fmum.config.client" )
@Config( modid = FMUM.MODID, category = "client" )
public final class ModConfigClient
{
	@LangKey( "fmum.config.client.use_flan_compatible_mousehelper" )
	@Comment(
		"The default mouse helper used in FMUM may conflict with the mouse "
		+ "helper used in Flan's Mod. This could cause bugs when you enter a "
		+ "vehicle in Flan's Mod. You can try to fix this issue by enabling "
		+ "this setting."
	)
	public static boolean use_flan_compatible_mousehelper = false;
	
	@RequiresMcRestart
	@LangKey( "fmum.config.client.default_creative_tab_icon_item" )
	@Comment(
		"This icon item will be used if FMUM fails to find icon item for a "
		+ "creative tab."
	)
	public static String default_creative_tab_icon_item
		= Items.FISH.getRegistryName().toString();
	
	@RequiresMcRestart
	@LangKey( "fmum.config.client.default_creative_tab_icon_item_meta" )
	@Comment(
		"See description of default_creative_tab_icon_item. "
		+ "This helps to decide the variant of the icon item."
	)
	public static short default_creative_tab_icon_item_meta = 0;
	
	private ModConfigClient() { }
}
