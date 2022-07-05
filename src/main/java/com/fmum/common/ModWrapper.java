package com.fmum.common;

import java.io.File;

import org.apache.logging.log4j.Logger;

import com.fmum.common.network.PacketHandler;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Wrapped launcher for {@link FMUM}
 * 
 * @see FMUM
 * @author Giant_Salted_Fish
 */
@Mod(
	modid = FMUM.MODID,
	name = FMUM.MOD_NAME,
	version = "@VERSION@"
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
	
	/**
	 * Network handler
	 */
	public static final PacketHandler net = new PacketHandler();
	
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
	
	/**
	 * Default logger for {@link FMUM}. Retrieved in {@link #onPreInit(FMLPreInitializationEvent)}.
	 * Can be acquired via {@link AutowireLogger}.
	 */
	public static Logger log = null;
	
	/**
	 * ".minecraft/" folder. Obtained in {@link #onPreInit(FMLPreInitializationEvent)}.
	 */
	protected File mcDir = null;
	
	/**
	 * Hook provided by Forge to do mod setup. Basically is calling methods from {@link #MOD} to do
	 * the setup according to the side.
	 */
	@EventHandler
	public void onPreInit( FMLPreInitializationEvent evt )
	{
		// Get logger
		log = evt.getModLog();
		
		// Get ".minecraft/" folder
		this.mcDir = evt.getModConfigurationDirectory().getParentFile();
		
		// Parse configuration settings
		MOD.loadConfig( new Configuration( evt.getSuggestedConfigurationFile() ) );
		
		// Retrieve logger and print info
		log.info( MOD.format( "fmum.onpreinitialization" ) );
		
		// Check OpenGL capability
		MOD.checkOpenGLCapability();
		
		// Load content packs
		MOD.loadContentPacks();
		
		log.info( MOD.format( "fmum.preinitializationcomplete" ) );
	}
	
	@EventHandler
	public void onInit( FMLInitializationEvent evt )
	{
		log.info( MOD.format( "fmum.oninitialization" ) );
		
		net.init();
		
		log.info( MOD.format( "fmum.initializationcomplete" ) );
		
		this.infoInitComplete();
	}
	
	@EventHandler
	public void onPostInit( FMLPostInitializationEvent evt )
	{
		log.info( MOD.format( "fmum.onpostinitialization" ) );
		
		net.postInit();
		
		MOD.loadKeyBinds();
		
		log.info( MOD.format( "fmum.postinitializationcomplete" ) );
	}
	
	protected void infoInitComplete() { }
	
	/**
	 * Provide logger that used for logging in {@link FMUM}
	 * 
	 * @author Giant_Salted_Fish
	 */
	public static interface AutowireLogger
	{
		public default Logger log() { return log; }
		
		/**
		 * You can call this to translate your message if the code will run on both side. Otherwise,
		 * simply use {@link I18n} if your code only runs on {@link Side#CLIENT} side.
		 * 
		 * @see I18n#format(String, Object...)
		 */
		public default String format( String translateKey, Object... parameters ) {
			return MOD.format( translateKey, parameters );
		}
	}
}
