package com.fmum.common;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.fmum.common.ModWrapper.AutowireLogger;
import com.fmum.common.meta.MetaBase;
import com.fmum.common.pack.ContentProvider;
import com.fmum.common.pack.FolderContentPack;
import com.fmum.common.pack.TypeCreativeTab;
import com.fmum.common.pack.ZipContentPack;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A weapon framework based on Flan's Mod and mainly focus on highly customizable guns. Modules for
 * vehicles, planes, mecha and other irrelevant staffs are removed to avoid massive work required to
 * maintain these frames. Almost everything left was reworked and refactored in order to simplify
 * the structure of the module and accomplish fancy features.
 * 
 * @author Giant_Salted_Fish
 * @credit
 *     jamioflan, FlansGames, W44, vinidamiani126, and everyone who else contributed to Flan's Mod.
 *     It is a mod that inspired me so mush ^_^
 */
public class FMUM extends ModWrapper implements ContentProvider, AutowireLogger
{
	/**
	 * Universal randomizer
	 */
	public static final Random rand = new Random();
	
	/**
	 * Easy referencing
	 */
	public static final Minecraft mc = Minecraft.getMinecraft();
	
	/**
	 * All content packs loaded by {@link FMUM}
	 */
	public static final TreeMap< String, ContentProvider > contentProviders = new TreeMap<>();
	
	public static final TypeCreativeTab tab = new TypeCreativeTab( MODID );
	
	/**
	 * Debug mode flag. Should be finalized in release version. Call {@link #toggleDebug()} instead
	 * of directly setting it to avoid having error in after finalized.
	 */
	public static boolean debug = false;
	public static boolean toggleDebug() {
		return debug = !debug;
	}
	
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
	
	protected static final Pattern SUFFIX_ZIP_JAR = Pattern.compile( "(.+)\\.(zip|jar)$" );
	
	/**
	 * For server side localization only
	 */
	@SideOnly( Side.SERVER )
	private HashMap<String, String> localization;
	
	public final void loadConfig( Configuration config )
	{
		// Load localization file for server side
		this.loadLocalizationFile(
			config.getString(
				"localizationFile",
				"Server Only",
				"en_us.lang",
				"Server side localization file"
			)
		);
		
		// Parse configuration
		this.parseConfig( config );
		
		// Save configuration file if has changed
		if( config.hasChanged() )
			config.save();
	}
	
	/**
	 * Client side only to check if the OpenGL version satisfies the requirement
	 */
	public void checkOpenGLCapability() { }
	
	public void loadContentPacks()
	{
		// Check content pack folder
		File packDir = new File( this.mcDir, this.packDirName );
		if( !packDir.exists() )
		{
			packDir.mkdirs();
			log.info( this.format( "fmum.packfoldercreated", this.packDirName ) );
		}
		
		// Find all content packs and load them
		final LinkedList< ContentProvider > providers = new LinkedList<>();
		for( File file : packDir.listFiles() )
		{
			ContentProvider provider;
			if( file.isDirectory() )
				provider = new FolderContentPack( file );
			else if( SUFFIX_ZIP_JAR.matcher( file.getName() ).matches() )
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
		{
			p.prepareLoad();
			log.info( this.format( "fmum.preloadcontentpack", p.sourceName() ) );
		}
		for( ContentProvider p : providers )
		{
			p.loadContent();
			contentProviders.put( p.name(), p );
			log.info( this.format( "fmum.loadedcontentpack", p.sourceName() ) );
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
	 * This localize the message based on the physical game side. Use this for localization if the
	 * message could be print in both side.
	 * 
	 * @see net.minecraft.client.resources.I18n#format(String, Object...)
	 */
	@Override
	public String format( String translateKey, Object... parameters )
	{
		String format = this.localization.get( translateKey );
		return String.format( format != null ? format : translateKey, parameters );
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
	
	/**
	 * Try to load localization file of standard format from given file path. Server side only.
	 */
	protected void loadLocalizationFile( String fName )
	{
		// TODO: load specified language file
		final HashMap< String, String > map = new HashMap<>();
		
		this.localization = map;
	}
	
	protected void parseConfig( Configuration config )
	{
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
	}
	
	@Override
	protected final void infoInitComplete()
	{
		log.info(
			this.format(
				"fmum.infoactivedpacks",
				Integer.toString( contentProviders.size() )
			)
		);
	}
	
//	void tick() { }
}