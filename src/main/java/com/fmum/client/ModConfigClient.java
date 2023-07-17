package com.fmum.client;

import com.fmum.common.FMUM;
import net.minecraft.init.Items;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.LangKey;
import net.minecraftforge.common.config.Config.RequiresMcRestart;

/**
 * Client only configurations.
 *
 * @author Giant_Salted_Fish
 */
//@SideOnly( Side.CLIENT ) // Commented as it will crash the load.
@LangKey( "fmum.config.client" )
@Config( modid = FMUM.MODID, category = "client" )
public final class ModConfigClient
{
	@LangKey( "fmum.config.client.use_flan_compatible_mousehelper" )
	@Comment(
		"The default mouse helper used in FMUM may conflict with the mouse helper used in Flan's "
			+ "Mod. This could cause bugs when you enter a vehicle in Flan's Mod. You can try to fix "
			+ "this issue by enabling this setting."
	)
	public static boolean useFlanCompatibleMouseHelper = false;
	
	@RequiresMcRestart
	@LangKey( "fmum.config.client.default_creative_tab_icon_item" )
	@Comment( "This icon item will be used if FMUM fails to find icon item for a creative tab." )
	public static String defaultCreativeTabIconItem = Items.FISH.getRegistryName().toString();
	
	private ModConfigClient() { }
}
