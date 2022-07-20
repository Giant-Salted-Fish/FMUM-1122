package com.fmum.common;

import java.io.File;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.Random;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.logging.log4j.Logger;

import com.fmum.client.render.ModelRepository;
import com.fmum.client.render.RenderableBase;
import com.fmum.common.meta.MetaBase;
import com.fmum.common.network.PacketHandler;
import com.fmum.common.pack.ContentProvider;
import com.fmum.common.pack.FolderContentPack;
import com.fmum.common.pack.MetaCreativeTab;
import com.fmum.common.pack.TypeCreativeTab;
import com.fmum.common.pack.ZipContentPack;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * A weapon framework based on Flan's Mod and mainly focus on highly customizable guns. Modules for
 * vehicles, planes, mecha and other irrelevant staffs are removed to avoid massive work required to
 * maintain these frames. Almost everything left was reworked and refactored in order to simplify
 * the structure of the module and accomplish fancy features.
 * 
 * @author Giant_Salted_Fish
 * @credit
 *     jamioflan, FlansGames, W44, vinidamiani126, and everyone who else contributed to Flan's Mod.
 *     It is a mod that inspired me so much ^_^
 */
public class FMUM extends ModWrapper implements ContentProvider, AutowireLogger
{
	/**
	 * Universal randomizer
	 */
	public static final Random rand = new Random();
	
//	MinecraftServer server = MinecraftServer.main( p_main_0_ );
	
	/**
	 * Network handler
	 */
	public static final PacketHandler net = new PacketHandler();
	
	/**
	 * All content packs loaded by {@link FMUM}
	 */
	public static final TreeMap< String, ContentProvider > contentProviders = new TreeMap<>();
	
	/**
	 * Debug mode flag. Should be finalized in release version. Call {@link #toggleDebug()} instead
	 * of directly setting it to avoid having error in after finalized.
	 */
	public static boolean debug = false;
	public static boolean toggleDebug() {
		return debug = !debug;
	}
	
	/**
	 * Default logger for {@link FMUM}. Set by {@link #onPreInit(FMLPreInitializationEvent)}. Can be
	 * acquired via {@link AutowireLogger}.
	 */
	protected static Logger log = null;
	
	/**
	 * @see #defCreativeTab()
	 */
	protected static TypeCreativeTab defTab = null;
	
	/**
	 * @see #hideCreativeTab()
	 */
	protected static MetaCreativeTab hideTab = null;
	
	/**
	 * Name of the folder that contains content packs to be loaded. In default is
	 * {@link ModWrapper#MODID}.
	 */
	public String packDirName = MODID;
	
	/**
	 * Max layers of the modules times 2(0-255 x 2 = 0-510)
	 */
	public int maxLocLen = 16;
	
	/**
	 * Max number of modules that can be installed in a single slot
	 */
	public int maxCanInstall = 5;
	
	/**
	 * ".minecraft/" folder. Obtained in {@link #onPreInit(FMLPreInitializationEvent)}.
	 */
	protected File mcDir = null;
	
	@Override
	public final void onPreInit( FMLPreInitializationEvent evt )
	{
		// Get logger
		log = evt.getModLog();
		log.info( this.format( "fmum.onpreinitialization" ) );
		
		// Setup default creative tab and hide creative tab
		( defTab = new TypeCreativeTab( MODID ) ).regisTo( defTab, MetaCreativeTab.regis );
		( hideTab = new MetaCreativeTab() {
			@Override
			public String name() { return "hide"; }
			
			@Override
			public CreativeTabs creativeTab() { return null; }
			
			@Override
			public String toString() { return this.identifier(); }
		} ).regisTo( hideTab, MetaCreativeTab.regis );
		
		// Get ".minecraft/" folder
		this.mcDir = evt.getModConfigurationDirectory().getParentFile();
		
		// Check OpenGL capability
		this.checkOpenGLCapability();
		
		// Parse configuration settings
		this.loadConfig( new Configuration( evt.getSuggestedConfigurationFile() ) );
		
		// Load content packs
		this.loadContentPacks();
		
		log.info( this.format( "fmum.preinitializationcomplete" ) );
	}
	
	@Override
	public final void onInit( FMLInitializationEvent evt )
	{
		log.info( this.format( "fmum.oninitialization" ) );
		
		net.init();
		
		log.info( this.format( "fmum.initializationcomplete" ) );
		
		this.infoInitComplete();
	}
	
	@Override
	public final void onPostInit( FMLPostInitializationEvent evt )
	{
		log.info( this.format( "fmum.onpostinitialization" ) );
		
		net.postInit();
		
		// FIXME: better initialize keys before we load custom keys
		this.loadKeyBinds();
		
		log.info( this.format( "fmum.postinitializationcomplete" ) );
	}
	
	/**
	 * Client side only to check if the OpenGL version satisfies the requirement
	 */
	public void checkOpenGLCapability() { }
	
	public void loadConfig( Configuration config )
	{
		log.info( this.format( "fmum.loadconfig" ) );
		
		// Parse common configuration
		final String CATEGORY = "Common";
		
		this.packDirName = config.getString(
			"contentPackFolder",
			CATEGORY,
			this.packDirName,
			"Content pack folder name where FMUM will load content packs from"
		);
		this.maxLocLen = config.getInt(
			"maxLayers",
			CATEGORY,
			this.maxLocLen >>> 1,
			1,
			255,
			"Max layers of modules can be installed on a base module"
		) << 1;
		this.maxCanInstall = config.getInt(
			"maxCanInstall",
			CATEGORY,
			this.maxCanInstall,
			1,
			254,
			"Max number of modules that can be installed in a single slot"
		);
		
		// Save configuration file if has changed TODO: check if this is needed
//		if( config.hasChanged() )
//			config.save();
	}
	
	public void loadContentPacks()
	{
		// Check content pack folder
		File packDir = new File( this.mcDir, this.packDirName );
		if( !packDir.exists() )
		{
			packDir.mkdirs();
			log.info( this.format( "fmum.packfoldercreated", this.packDirName ) );
		}
		
		// Compile a regex to match the supported content pack file types
		final Pattern packPattern = Pattern.compile( "(.+)\\.(zip|jar)$" );
		
		// Find all content packs and load them
		final LinkedList< ContentProvider > providers = new LinkedList<>();
		for( File file : packDir.listFiles() )
		{
			ContentProvider provider;
			if( file.isDirectory() )
				provider = new FolderContentPack( file );
			else if( packPattern.matcher( file.getName() ).matches() )
				provider = new ZipContentPack( file );
			else
			{
				log.warn(
					this.format(
						"fmum.unknowncontentpackfiletype",
						packDir.getName() + "/" + file.getName()
					)
				);
				continue;
			}
			
			providers.add( provider );
			log.info( this.format( "fmum.detectcontentpack", provider.sourceName() ) );
		}
		
		// Load content packs!
		for( ContentProvider p : providers )
			p.prepareLoad();
		for( ContentProvider p : providers )
		{
			log.info( this.format( "fmum.loadcontentpack", p.sourceName() ) );
			p.loadContent();
			contentProviders.put( p.name(), p );
		}
		
		// Fire post load event
		for( MetaBase mb : MetaBase.regis.values() )
			mb.onPostLoad();
	}
	
	/**
	 * Called for client side to trigger key lazy load
	 */
	public void loadKeyBinds() { }
	
	public void regisLocalResource( File source )
	{
		try { FMUMClassLoader.INSTANCE.addURL( source.toURI().toURL() ); }
		catch( MalformedURLException e ) {
			log.error( this.format( "fmum.erroraddingclasspath", source.getName() ), e );
		}
	}
	
	@Nullable
	public ResourceLocation loadTexture( String path ) { return null; }
	
	/**
	 * In default server side will not load models. But it is reserved as a capability that once
	 * someday in the future the data in models are needed then this will simply work.
	 * 
	 * @see ModelRepository
	 * @param
	 *     path Path of the model. Format is {@code repoName + ":" + modelName}. If {@code repoName}
	 *     does not present then {@code modelName} should be the class path of the model to load. As
	 *     for {@code repoName} see {@link ModelRepository}.
	 */
	public RenderableBase loadModel( String path ) { return null; }
	
	public final MetaCreativeTab defCreativeTab() { return defTab; }
	
	public final MetaCreativeTab hideCreativeTab() { return hideTab; }
	
	public Side side() { return Side.SERVER; }
	
	public boolean isClient() { return false; }
	
	/**
	 * This localize the message based on the physical side. Use this for localization if the
	 * message will should in both client side and server side.
	 * 
	 * @see net.minecraft.client.resources.I18n#format(String, Object...)
	 */
	@Override
	public String format( String translateKey, Object... parameters ) {
		return I18n.translateToLocalFormatted( translateKey, parameters );
	}
	
	@Override
	public String name() { return "fmum.pack"; }
	
	@Override
	public String author() { return "fmum.author"; }
	
	@Override
	public String description() { return "fmum.description"; }
	
	@Override
	public String sourceName() { return MOD_NAME; }
	
	@Override
	public Object meta( String key )
	{
		switch( key )
		{
		case "modid": return FMUM.MODID;
		case "name": return FMUM.MOD_NAME;
		case "version": return "2.0"; // TODO: version from .mcmod?
		case "author": return "Giant_Salted_Fish";
		case "description": return "A hardcore gun mod";
		default: return null;
		}
	}
	
	final void infoInitComplete()
	{
		log.info( this.format(
			"fmum.infoactivatedpacks",
			Integer.toString( contentProviders.size() )
		) );
		for( ContentProvider cp : contentProviders.values() )
			log.info( this.format(
				"fmum.activatedpackinfo",
				this.format( cp.name() ),
				this.format( cp.author() )
			) );
	}
	
//	protected void tick() { }
}