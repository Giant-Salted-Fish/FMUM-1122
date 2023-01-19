package com.mcwb.client;

import com.mcwb.common.MCWB;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.LangKey;

//@SideOnly( Side.CLIENT ) // Commented as it will crash on load
@LangKey( "mcwb.config.client" )
@Config( modid = MCWB.MODID, category = "client" )
public final class ModConfigClient
{
}
