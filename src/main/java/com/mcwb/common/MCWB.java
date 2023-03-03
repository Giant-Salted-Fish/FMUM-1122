package com.mcwb.common;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.mcwb.common.gun.GunPartType;
import com.mcwb.common.gun.GunType;
import com.mcwb.common.load.BuildableLoader;
import com.mcwb.common.load.IContentProvider;
import com.mcwb.common.load.IMeshLoadSubscriber;
import com.mcwb.common.load.IPostLoadSubscriber;
import com.mcwb.common.meta.IContexted;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.meta.Registry;
import com.mcwb.common.module.IModuleSlot;
import com.mcwb.common.module.RailSlot;
import com.mcwb.common.network.PacketHandler;
import com.mcwb.common.pack.FolderPack;
import com.mcwb.common.pack.JarPack;
import com.mcwb.common.pack.LocalPack;
import com.mcwb.common.paintjob.IPaintjob;
import com.mcwb.common.paintjob.Paintjob;
import com.mcwb.common.player.PlayerPatch;
import com.mcwb.common.tab.CreativeTab;
import com.mcwb.util.AngleAxis4f;
import com.mcwb.util.Quat4f;
import com.mcwb.util.Vec3f;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.nbt.NBTBase;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * A weapon mod that provides highly customizable weapons
 * 
 * @author Giant_Salted_Fish
 */
@Mod(
	modid = MCWB.ID,
	name = MCWB.NAME,
	version = MCWB.VERSION,
	acceptedMinecraftVersions = "[1.12, 1.13)",
	guiFactory = "com.mcwb.client.ConfigGuiFactory"
//	, clientSideOnly = true
)
public class MCWB extends URLClassLoader
	implements IContentProvider, IAutowirePacketHandler, IAutowireLogger
{
	/**
	 * Mod id
	 */
	public static final String ID = "mcwb";
	
	/**
	 * A human friendly mod name
	 */
	public static final String NAME = "Minecraft Weapon Base";
	
	/**
	 * Current mod version
	 * TODO: update version before publish
	 */
	public static final String VERSION = "0.2.0-alpha";
	
	/**
	 * Instantiate the mod instance by side to handle the side dependent content load
	 */
	public static final MCWB MOD;
	static
	{
		if( FMLCommonHandler.instance().getSide().isServer() )
			MOD = new MCWB();
		else try
		{
			// Avoid class not define exception with indirect reference
			MOD = ( MCWB ) Class.forName( "com.mcwb.client.MCWBClient" )
				.getField( "MOD" ).get( null );
		}
		catch( Exception e ) {
			throw new RuntimeException( "Can not get client proxy. Should be impossible", e );
		}
	}
	@Mod.InstanceFactory
	public static MCWB instance() { return MOD; }
	
	/**
	 * A default {@link Gson} parser to load types in content pack
	 */
	public static final Gson GSON = MOD.gsonBuilder().create();
	
	/**
	 * Type loaders
	 */
	public static final Registry< BuildableLoader< ? extends IMeta > >
		TYPE_LOADERS = new Registry<>();
	
	/**
	 * Default creative item tab
	 */
	public static final CreativeTab DEFAULT_TAB = new CreativeTab().build( ID, MOD );
	
	/**
	 * Items added into this tab will not be hidden from creative item tab
	 */
	public static final CreativeTab HIDE_TAB = new CreativeTab() {
		@Override
		protected CreativeTabs createTab() { return null; }
	}.build( "hide", MOD );
	
	/**
	 * Loaded content packs
	 */
	public static final Registry< IContentProvider > CONTENT_PROVIDERS = new Registry<>();
	
	/**
	 * @see IAutowirePacketHandler
	 */
	public static final PacketHandler NET = new PacketHandler( ID );
	
	/**
	 * @see IAutowireLogger
	 */
	public static final Logger LOGGER = LogManager.getLogger( ID );
	
	// FIXME: Initialize this for pure server side, check this on server side independently?
	public static int maxSlotCapacity; // TODO: why not directly refer it from mod config on server side?
	
	/**
	 * ".minecraft/" folder
	 */
	protected final File gameDir = Loader.instance().getConfigDir().getParentFile();
	
	/**
	 * Buffered sounds
	 */
	final HashMap< String, SoundEvent > soundPool = new HashMap<>();
	
	/**
	 * Registered subscribers for post load callback
	 */
	private final LinkedList< IPostLoadSubscriber > postLoadSubscribers = new LinkedList<>();
	
//	private final HashMap< String, byte[] > classes = new HashMap<>();
	
	protected MCWB() { super( new URL[ 0 ], MinecraftServer.class.getClassLoader() ); }
	
	@EventHandler
	public final void onPreInit( FMLPreInitializationEvent evt )
	{
		// Info load start
		this.info( "mcwb.on_pre_init" );
		
		// Prepare pack load
		this.preLoad();
		
		// Load content packs
		this.load();
		
		this.info( "mcwb.pre_init_complete" );
	}
	
	@EventHandler
	public final void onInit( FMLInitializationEvent evt )
	{
		this.info( "mcwb.on_init" );
		
		// Fire load callback
		this.postLoadSubscribers.forEach( IPostLoadSubscriber::onPostLoad );
		this.postLoadSubscribers.clear();
		
		NET.regisPackets();
		
		this.info( "mcwb.init_complete" );
		
		// After initialization, info user all the packs being loaded
		this.info( "mcwb.total_loaded_packs", "" + CONTENT_PROVIDERS.size() );
		CONTENT_PROVIDERS.values().forEach(
			p -> this.info(
				"mcwb.info_loaded_pack",
				this.format( p.name() ),
				this.format( p.author() )
			)
		);
	}
	
	@EventHandler
	public final void onPostInit( FMLPostInitializationEvent evt )
	{
		this.info( "mcwb.on_post_init" );
		
		// may add possibility to register packets for packs?
//		NET.postInit();
		
		this.info( "mcwb.post_init_complete" );
	}
	
	@Override
	public void preLoad()
	{
		// Register capabilities
		this.regisCapability( PlayerPatch.class );
		this.regisCapability( IContexted.class );
		
		// Register meta loaders
		TYPE_LOADERS.regis( CreativeTab.LOADER );
		TYPE_LOADERS.regis( GunPartType.LOADER );
		TYPE_LOADERS.regis( GunType.LOADER );
//		TYPE_LOADERS.regis( MagType.LOADER );
//		TYPE_LOADERS.regis( AmmoType.LOADER );
		TYPE_LOADERS.regis( Paintjob.LOADER );
		this.regisSideDependentLoaders();
	}
	
	@Override
	public void load()
	{
		// Check content pack folder
		// TODO: if load packs from mods dir then allow the player to disable content pack folder
		final File packDir = new File( this.gameDir, ID );
		if( !packDir.exists() )
		{
			packDir.mkdirs();
			this.info( "mcwb.pack_dir_created", ID );
		}
		
		// Compile a regex to match the supported content pack file types
		// TODO: check if zip is supported
		final Pattern jarRegex = Pattern.compile( "(.+)\\.(zip|jar)$" );
		
		// Find all content packs and load them
		final HashSet< IContentProvider > providers = new HashSet<>();
		for( final File file : packDir.listFiles() )
		{
			LocalPack pack;
			if( file.isDirectory() )
				pack = new FolderPack( file );
			else if( jarRegex.matcher( file.getName() ).matches() )
				pack = new JarPack( file );
			else
			{
				this.warn(
					"mcwb.unknown_pack_file_type",
					packDir.getName() + "/" + file.getName()
				);
				continue;
			}
			
			providers.add( pack );
			this.info( "mcwb.detect_content_pack", file.getName() );
		}
		
		// TODO: Post provider registry event to get providers from other mods?
//		MinecraftForge.EVENT_BUS.post( new ContentProviderRegistryEvent( providers ) );
		
		// Let content packs register resource domains and reload Minecraft resources
		providers.forEach( IContentProvider::preLoad );
		this.reloadResources(); // TODO: check if it works without this
		
		// Load content packs!
		providers.forEach( p -> {
			this.info( "mcwb.load_content_pack", p.sourceName() );
			p.load();
			CONTENT_PROVIDERS.regis( p );
		} );
	}
	
	public void addResourceDomain( File file )
	{
		try { this.addURL( file.toURI().toURL() ); }
		catch( MalformedURLException e ) {
			this.except( e, "mcwb.error_adding_classpath", file.getName() );
		}
	}
	
	public final < T > void regisCapability( Class< T > capabilityClass )
	{
		CapabilityManager.INSTANCE.register(
			capabilityClass,
			new Capability.IStorage< T >()
			{
				@Nullable
				@Override
				public NBTBase writeNBT( Capability< T > capability, T instance, EnumFacing side ) {
					return null;
				}
				
				@Override
				public void readNBT(
					Capability< T > capability,
					T instance,
					EnumFacing side,
					NBTBase nbt
				) { }
			},
			() -> null
		);
	}
	
	@Override
	public void regis( IPostLoadSubscriber subscriber ) {
		this.postLoadSubscribers.add( subscriber );
	}
	
	@Override
	public void regis( IMeshLoadSubscriber subscriber ) { }
	
	@Override
	public SoundEvent loadSound( String path )
	{
		return this.soundPool.computeIfAbsent( path, key -> {
			final MCWBResource res = new MCWBResource( path );
			return new SoundEvent( res ).setRegistryName( res );
		} );
	}
	
	@Override
	public boolean isClient() { return false; }
	
	@Override
	public void clientOnly( Runnable task ) { }
	
	@Override
	@SuppressWarnings( "deprecation" )
	public String format( String translateKey, Object... parameters )
	{
		return net.minecraft.util.text.translation
			.I18n.translateToLocalFormatted( translateKey, parameters );
	}
	
	@Override
	public String name() { return "mcwb.core_pack"; }
	
	@Override
	public String author() { return "mcwb.author"; }
	
	@Override
	public String sourceName() { return NAME; }
	
	@Override
	public String toString() { return ID; }
	
	protected void regisSideDependentLoaders()
	{
		// Key binds is client only. But to avoid the errors when it is absent, set a loader that \
		// simply does nothing on load.
		TYPE_LOADERS.regis( new BuildableLoader<>(
			"key_bind", json -> ( name, provider ) -> null
		) );
	}
	
	/**
	 * client only to reload resources
	 * 
	 * TODO: check if needed server side to load languages
	 */
	protected void reloadResources() { }
	
	protected GsonBuilder gsonBuilder()
	{
		final GsonBuilder builder = new GsonBuilder();
		builder.setLenient();
		builder.setPrettyPrinting();
		
		builder.registerTypeAdapter( IModuleSlot.class, RailSlot.ADAPTER );
		builder.registerTypeAdapter( IPaintjob.class, Paintjob.ADAPTER );
//		builder.registerTypeAdapter( IOperationController.class, OperationController.ADAPTER );
		
		final JsonDeserializer< SoundEvent > soundAdapter =
			( json, typeOfT, context ) -> this.loadSound( json.getAsString() );
		builder.registerTypeAdapter( SoundEvent.class, soundAdapter );
		
		final Gson innerParser = new GsonBuilder().setLenient().create();
		final JsonDeserializer< Vec3f > vecAdapter = ( json, typeOfT, context ) -> {
			if( json.isJsonArray() )
			{
				final JsonArray arr = json.getAsJsonArray();
				final Vec3f vec = new Vec3f();
				switch( arr.size() )
				{
				case 3: vec.z = arr.get( 2 ).getAsFloat();
				case 2: vec.y = arr.get( 1 ).getAsFloat();
				case 1: vec.x = arr.get( 0 ).getAsFloat();
				}
				return vec;
			}
			
			return json.isJsonObject() ? innerParser.fromJson( json, Vec3f.class )
				: Vec3f.parse( json.getAsString() );
		};
		final JsonDeserializer< AngleAxis4f > angleAxisAdapter = ( json, typeOfT, context ) -> {
			if( !json.isJsonArray() )
				return innerParser.fromJson( json, AngleAxis4f.class );
			
			final JsonArray arr = json.getAsJsonArray();
			final float f0 = arr.get( 0 ).getAsFloat();
			final float f1 = arr.get( 1 ).getAsFloat();
			final float f2 = arr.get( 2 ).getAsFloat();
			return arr.size() < 4 ? new AngleAxis4f( f0, f1, f2 )
				: new AngleAxis4f( f0, f1, f2, arr.get( 3 ).getAsFloat() );
		};
		final JsonDeserializer< Quat4f > quatAdapter = ( json, typeOfT, context ) -> 
			new Quat4f( angleAxisAdapter.deserialize( json, typeOfT, context ) );
		builder.registerTypeAdapter( Vec3f.class, vecAdapter );
		builder.registerTypeAdapter( AngleAxis4f.class, angleAxisAdapter );
		builder.registerTypeAdapter( Quat4f.class, quatAdapter );
		return builder;
	}
	
//	@Override
//	protected Class< ? > findClass( String name ) throws ClassNotFoundException
//	{
//		final byte[] bytes = this.classes.get( name );
//		return(
//			bytes != null
//			? this.defineClass( name, bytes, 0, bytes.length )
//			: super.findClass( name )
//		);
//	}
}
