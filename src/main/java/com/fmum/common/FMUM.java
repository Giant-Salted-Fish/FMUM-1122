package com.fmum.common;

import java.util.Random;
import java.util.TreeSet;

import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
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
	
	/**
	 * Transform between radians and degrees
	 */
	public static final float
		TO_RADIANS = (float)Math.PI / 180F,
		TO_DEGREES = 180F / (float)Math.PI;
	
	public static final String
		TXT_FILE_SUFFIX = ".txt",
		CLASS_FILE_SUFFIX = ".class";
	
	/**
	 * Easy referencing
	 */
	public static final Minecraft mc = Minecraft.getMinecraft();
	
	/**
	 * Some fixed empty containers that can be used as initializer value
	 */
	public static final TreeSet<String> EMPTY_STR_SET = new TreeSet<>();
	
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
	 * Debug mode flag. Should be finalized in release version. Call {@link #toggleDebug()} to
	 * change debug state.
	 */
	public static boolean debug = false;
	public static void toggleDebug() { debug = !debug; }
	
	/**
	 * Universal ticker
	 */
	public static int ticker = 0;
	
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
		
		log.info(proxy.format("fmum.oninitializationcomplete"));
		
		proxy.initComplete();
	}
	
	/**
	 * Try to load required class and instantiate it. Prints the error if any has occurred. The
	 * input raw path fragments will be processed by {@link #spliceClassPath(String...)}.
	 * 
	 * @param pathFragments class path fragments to the required class
	 */
	public static Object tryInstantiate(String... pathFragments)
	{
		pathFragments[0] = spliceClassPath(pathFragments);
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
	
	public static String spliceClassPath(String... pathFragments)
	{
		String classPath = pathFragments[pathFragments.length - 1];
		for(
			int i = pathFragments.length - 1;
			--i >= 0;
			classPath = pathFragments[i] + "." + classPath
		);
		return(
			classPath.endsWith(CLASS_FILE_SUFFIX)
			? classPath.substring(0, classPath.length() - CLASS_FILE_SUFFIX.length())
			: classPath
		);
	}
	
	public static String splice(String[] split, int head) {
		return splice(split, head, split.length);
	}
	
	public static String splice(String[] split, int head, int tail)
	{
		String s = "";
		while(--tail >= head) s = split[tail] + " " + s;
		return s;
	}
}