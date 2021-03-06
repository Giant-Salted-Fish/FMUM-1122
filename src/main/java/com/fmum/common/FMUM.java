package com.fmum.common;

import java.util.Random;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;

import com.fmum.common.network.PacketHandler;
import com.fmum.common.util.Util;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * A weapon framework based on Flan's Mod and mainly focus on highly customizable guns. Modules for
 * vehicles, planes, mecha and other irrelevant staffs are removed to avoid massive work required to
 * maintain these frames. Almost everything left was reworked and refactored in order to simplify
 * the structure of the module and accomplish fancy features.
 * 
 * @author Giant_Salted_Fish
 * @credit
 *     jamioflan, FlansGames, W44, vinidamiani126, and everyone who else contributed in building
 *     Flan's Mod framework which inspired me so intensively ^_^
 */
@Mod(
	modid = FMUM.MODID,
	name = FMUM.MOD_NAME,
	version = "@VERSION@"
//	, guiFactory = "com.flansmod.client.gui.config.ModGuiFactory"
//	, clientSideOnly = true
)
public final class FMUM
{
	/**
	 * Universal randomizer
	 */
	public static final Random rand = new Random();
	
	public static final Pattern
		INTEGER_FORMAT = Pattern.compile("-?[0-9]+"),
		REAL_NUMBER_FORMAT = Pattern.compile("-?[0-9]+\\.?[0-9]*");
	
	/**
	 * Easy referencing
	 */
	public static final Minecraft mc = Minecraft.getMinecraft();
	
	/**
	 * Id of {@link FMUM}
	 */
	public static final String MODID = "fmum";
	
	/**
	 * A user friendly name of {@link FMUM}
	 */
	public static final String MOD_NAME = "Flan's Mod Ultimate 1.1 Modified";
	
	/**
	 * Instance of the {@link FMUM}
	 */
	@Instance(MODID)
	public static FMUM INSTANCE;
	
	/**
	 * Side based loader agent
	 */
	@SidedProxy(
		clientSide = "com.fmum.client.ClientProxy",
		serverSide = "com.fmum.common.CommonProxy"
	)
	public static CommonProxy proxy;
	
	/**
	 * Default logger for {@link FMUM}
	 */
	public static Logger log = null;

	/**
	 * Network handler
	 */
	public static final PacketHandler netHandler = new PacketHandler();
	
	/**
	 * Debug mode flag. Should be finalized in release version. Call {@link #toggleDebug()} to
	 * change debug state.
	 */
	public static boolean debug = false;
	public static void toggleDebug() { debug = !debug; }
	
	@EventHandler
	public void onPreInit(FMLPreInitializationEvent evt)
	{
		// Parse configuration
		proxy.syncConfig(
			new Configuration(evt.getSuggestedConfigurationFile()),
			evt.getModConfigurationDirectory().getParentFile()
		);
		
		proxy.loadLocalizationMap();
		
		log = evt.getModLog();
		log.info(proxy.format("fmum.onpreinitialization"));
		
		// Check OpenGL version
		proxy.checkOpenGL();
		
		// Load content packs
		proxy.loadContentPack();
		
		log.info(proxy.format("fmum.preinitializationcomplete"));
	}
	
	@EventHandler
	public void onInit(FMLInitializationEvent evt)
	{
		log.info(proxy.format("fmum.oninitialization"));
		
		proxy.setupCreativeTabs();
		netHandler.init();
		
		log.info(proxy.format("fmum.initializationcomplete"));
		
		proxy.initComplete();
	}
	
	@EventHandler
	public void onPostInit(FMLPostInitializationEvent evt)
	{
		log.info(proxy.format("fmum.onpostinitialization"));
		
		netHandler.postInit();
		
		proxy.loadKeyBinds();
		
		log.info(proxy.format("fmum.postinitializationcomplete"));
	}
	
	/**
	 * Try to load required class and instantiate it. Prints the error if any has occurred. The
	 * input raw path fragments will be processed by {@link #spliceClassPath(String...)}.
	 * 
	 * @param pathFragments class path fragments to the required class
	 */
	public static Object tryInstantiate(String... pathFragments)
	{
		pathFragments[0] = Util.spliceClassPath(pathFragments);
		try
		{
			return FMUMClassLoader.INSTANCE.loadClass(
				pathFragments[0]
			).getConstructor().newInstance();
		}
		catch(Exception e) {
			FMUM.log.error(proxy.format("fmum.errorinstantiating", pathFragments[0]), e);
		}
		return null;
	}
}