package com.fmum.common.pack;

import java.util.HashMap;

import com.fmum.common.FMUM;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public final class FMUMCreativeTab extends CreativeTabs
{
	public static final HashMap<String, FMUMCreativeTab> tabs = new HashMap<>();
	
	public static final String RECOMMENDED_SOURCE_DIR_NAME = "tab";
	
	/**
	 * Default creative item tab for {@link FMUM}
	 */
	public static final FMUMCreativeTab INSTANCE = new FMUMCreativeTab(FMUM.MODID, FMUM.MOD_NAME);
	
	public final String contentPackName;
	
	public FMUMCreativeTab(String label, String contentPackName)
	{
		super(label);
		
		this.contentPackName = contentPackName;
		tabs.put(label, this);
	}
	
	@Override
	public ItemStack createIcon()
	{
		return new ItemStack(Blocks.WOOL, 1, 10);
	}
	
	@Override
	public String toString() { return "tab:" + this.getTabLabel(); }
}