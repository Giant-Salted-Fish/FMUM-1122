package com.mcwb.client;

import java.util.Collections;
import java.util.Set;

import com.mcwb.common.MCWB;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public final class ConfigGuiFactory implements IModGuiFactory
{
	@Override
	public void initialize( Minecraft mc ) { }
	
	@Override
	public boolean hasConfigGui() { return true; }
	
	@Override
	public GuiScreen createConfigGui( GuiScreen parent ) {
		return new GuiConfig( parent, MCWB.MODID, "mcwb.cfg" );
	}
	
	@Override
	public Set< RuntimeOptionCategoryElement > runtimeGuiCategories() {
		return Collections.emptySet();
	}
}
