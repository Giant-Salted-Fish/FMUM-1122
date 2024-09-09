package com.fmum.tab;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.Optional;

public final class CreativeTabUtil
{
	@SideOnly( Side.CLIENT )
	public static Optional< CreativeTabs > lookup( String label )
	{
		return (
			Arrays.stream( CreativeTabs.CREATIVE_TAB_ARRAY )
			.filter( tab -> tab.getTabLabel().equals( label ) )
			.findFirst()
		);
	}
	
	private CreativeTabUtil() { }
}
