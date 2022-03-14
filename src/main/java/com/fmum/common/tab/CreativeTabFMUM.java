package com.fmum.common.tab;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public final class CreativeTabFMUM extends CreativeTabs
{
	public CreativeTabFMUM(String name)
	{
		super(name);
	}
	
	public ItemStack createIcon()
	{
		return new ItemStack(Blocks.WOOL, 1, 10);
	}
}