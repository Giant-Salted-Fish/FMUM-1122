//package com.fmum;
//
//import network.minecraft.client.Minecraft;
//import network.minecraft.client.gui.GuiScreen;
//import network.minecraftforge.fml.client.IModGuiFactory;
//import network.minecraftforge.fml.client.config.GuiConfig;
//import network.minecraftforge.fml.relauncher.Side;
//import network.minecraftforge.fml.relauncher.SideOnly;
//
//import java.util.Collections;
//import java.util.Set;
//
///**
// * Reference: <a href="https://harbinger.covertdragon.team/chapter-26/config-gui.html">
// *     Harbinger: Chapter-26.1 </a>
// */
//@SideOnly( Side.CLIENT )
//public final class ConfigGuiFactory implements IModGuiFactory
//{
//	@Override
//	public void initialize( Minecraft mc ) {
//		// Pass.
//	}
//
//	@Override
//	public boolean hasConfigGui() {
//		return true;
//	}
//
//	@Override
//	public GuiScreen createConfigGui( GuiScreen parent ) {
//		return new GuiConfig( parent, FMUM.MODID, "fmum.cfg" );
//	}
//
//	@Override
//	public Set< RuntimeOptionCategoryElement > runtimeGuiCategories() {
//		return Collections.emptySet();
//	}
//}
