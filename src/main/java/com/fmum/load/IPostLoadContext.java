package com.fmum.load;

import com.google.gson.Gson;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IPostLoadContext
{
	Gson getGson();
	
	/**
	 * Only call this if you really need the fallback tab. The fallback tab will
	 * not be created if no one called this function to avoid the display of an
	 * empty tab.
	 */
	CreativeTabs getFallbackCreativeTab();
	
	@SideOnly( Side.CLIENT )
	ItemStack getFallbackTabIconItem();
}
