package com.fmum.common;

import java.io.File;
import java.util.Random;

import org.apache.logging.log4j.Logger;

import com.fmum.common.tab.CreativeTabFMUM;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

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
	 * Name of the folder that contains content packs to be loaded
	 */
	public static String packDirName = MODID;
	
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
	
	/**
	 * Test tab for fmum TODO: remove this
	 */
	public static final CreativeTabFMUM tab = new CreativeTabFMUM("tabfmum");
	
	@EventHandler
	public void onPreInit(FMLPreInitializationEvent evt)
	{
		log = evt.getModLog();
		log.info(I18n.format("fmum.onpreinitialization"));
		
		// Parse FMUM configuration
		this.syncConfig(new Configuration(evt.getSuggestedConfigurationFile()));
		
		// Check content pack folder
		File packDir = new File(evt.getModConfigurationDirectory().getParentFile(), packDirName);
		if(!packDir.exists())
		{
			packDir.mkdirs();
			log.info(I18n.format("fmum.packfoldercreated", packDirName));
		}
		
		// Load content packs
		proxy.loadContentPack(packDir);
		
		log.info(I18n.format("fmum.preinitializationcomplete"));
	}

	@EventHandler
	public void onInit(FMLInitializationEvent evt)
	{
		log.info(I18n.format("fmum.oninitialization"));
		
		log.info(I18n.format("fmum.oninitializationcomplete"));
	}
	
	private void syncConfig(Configuration config)
	{
		final String COMMON_SETTING = "FMUM Common Settings";
		
		packDirName = config.getString(
			"packFolderName",
			COMMON_SETTING,
			packDirName,
			"Content pack folder name where FMUM will load content packs from"
		);
		
		// Save configuration file if has changed
		if(config.hasChanged())
			config.save();
	}
}