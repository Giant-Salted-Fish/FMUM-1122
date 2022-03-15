package com.fmum.common.pack;

import java.util.HashMap;

import com.fmum.common.FMUM;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public final class FMUMCreativeTab extends CreativeTabs
{
	public static final HashMap<String, FMUMCreativeTab> tabs = new HashMap<>();
	
	/**
	 * Default creative item tab for {@link FMUM}
	 */
	public static final FMUMCreativeTab INSTANCE = new FMUMCreativeTab(FMUM.MODID, FMUM.MOD_NAME);
	
	public final String contentPackName;
	
	public FMUMCreativeTab(String name, String contentPackName)
	{
		super(name);
		
		this.contentPackName = contentPackName;
		tabs.put(name, this);
	}
	
	@Override
	public ItemStack createIcon()
	{
		return new ItemStack(Blocks.WOOL, 1, 10);
	}
	
	@Override
	public String toString() { return "tab:" + this.getTabLabel(); }
}