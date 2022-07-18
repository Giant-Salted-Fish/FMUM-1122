package com.fmum.common;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * Wrapped launcher for {@link FMUM}
 * 
 * @see FMUM
 * @author Giant_Salted_Fish
 */
@Mod(
	modid = FMUM.MODID,
	name = FMUM.MOD_NAME,
	version = FMUM.MOD_VERSION
//	, guiFactory = "com.fmum.client.gui.config.ModGuiFactory"
//	, clientSideOnly = true
)
public class ModWrapper
{
	/**
	 * Id of {@link FMUM}
	 */
	public static final String MODID = "fmum";
	
	/**
	 * A user friendly name for {@link FMUM}
	 */
	public static final String MOD_NAME = "Flan's Mod Ultimate 1.1 Modified";
	
	public static final String MOD_VERSION = "2.0";
	
	/**
	 * Although this is the class with {@link Mod} annotation, it actually behaves more like a
	 * wrapped launcher of the real mod instance which is referred by {@link #MOD}
	 */
	@Instance( MODID )
	public static ModWrapper LAUNCHER;
	
	/**
	 * Actual mod instance. Implemented differ by side.
	 */
	@SidedProxy(
		serverSide = "com.fmum.common.FMUM",
		clientSide = "com.fmum.client.FMUMClient"
	)
	public static FMUM MOD;
	
	@EventHandler
	public void onPreInit( FMLPreInitializationEvent evt ) { MOD.onPreInit( evt ); }
	
	@EventHandler
	public void onInit( FMLInitializationEvent evt ) { MOD.onInit( evt ); }
	
	@EventHandler
	public void onPostInit( FMLPostInitializationEvent evt ) { MOD.onPostInit( evt ); }
}
