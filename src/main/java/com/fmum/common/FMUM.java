package com.fmum.common;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fmum.client.FMUMClient;
import com.fmum.common.ammo.JsonAmmoType;
import com.fmum.common.gun.JsonGunPartType;
import com.fmum.common.gun.JsonGunType;
import com.fmum.common.gun.JsonMagType;
import com.fmum.common.item.IItem;
import com.fmum.common.item.IItemType;
import com.fmum.common.load.BuildableLoader;
import com.fmum.common.load.IContentProvider;
import com.fmum.common.load.IMeshLoadSubscriber;
import com.fmum.common.load.IPostLoadSubscriber;
import com.fmum.common.meta.IMeta;
import com.fmum.common.meta.Registry;
import com.fmum.common.module.IModuleSlot;
import com.fmum.common.module.ModuleCategory;
import com.fmum.common.module.ModuleFilter;
import com.fmum.common.module.RailSlot;
import com.fmum.common.network.PacketHandler;
import com.fmum.common.pack.FolderPack;
import com.fmum.common.pack.JarPack;
import com.fmum.common.paintjob.IPaintjob;
import com.fmum.common.paintjob.JsonPaintjob;
import com.fmum.common.paintjob.Paintjob;
import com.fmum.common.player.IOperationController;
import com.fmum.common.player.OperationController;
import com.fmum.common.player.PlayerPatch;
import com.fmum.common.tab.CreativeTab;
import com.fmum.util.AngleAxis4f;
import com.fmum.util.Quat4f;
import com.fmum.util.Vec3f;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.nbt.NBTBase;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * A weapon mod that as a platform to supply highly customizable weapons.
 * 
 * @author Giant_Salted_Fish
 */
@Mod(
	modid = FMUM.MODID,
	name = FMUM.MOD_NAME,
	version = FMUM.MOD_VERSION,
	acceptedMinecraftVersions = "[1.12, 1.13)",
	guiFactory = "com.fmum.client.ConfigGuiFactory"
//	, clientSideOnly = true
)
public class FMUM extends URLClassLoader
	implements IContentProvider, IAutowirePacketHandler, IAutowireLogger
{
	public static final String MODID = "fmum";
	
	public static final String MOD_NAME = "FMUM 2.0";
	
	public static final String MOD_VERSION = "0.3.1-alpha";
	
	/**
	 * Mod instance based on physical side.
	 */
	public static final FMUM MOD;
	static
	{
		final Supplier< ? > client = () -> FMUMClient.MOD; // Use generic will crash.
		final Side side = FMLCommonHandler.instance().getSide();
		MOD = side.isServer() ? new FMUM() : ( FMUM ) client.get();
	}
	@Mod.InstanceFactory
	public static FMUM instance() { return MOD; }
	
	/**
	 * Default json parser to load types in content pack.
	 */
	public static final Gson GSON = MOD.gsonBuilder().create();
	
	/**
	 * Registered type loaders.
	 */
	public static final Registry< BuildableLoader< ? extends IMeta > >
		TYPE_LOADERS = new Registry<>();
	
	/**
	 * Default creative item tab for {@link FMUM}.
	 */
	public static final CreativeTab DEFAULT_TAB = new CreativeTab()
	{
		Runnable setup = () -> {
			this.build( MODID, MOD );
			this.setup = () -> { };
		};
		
		@Override
		public void itemSettledIn( IItemType item )
		{
			this.setup.run();
			super.itemSettledIn( item );
		}
	};
	
	/**
	 * Items added into this tab will be hidden from creative mode item tab.
	 */
	public static final CreativeTab HIDE_TAB = new CreativeTab() {
		@Override
		protected CreativeTabs createTab() { return null; }
	}.build( "hide", MOD );
	
	// Has to put it here as current implementation will register a new item for it.
	public static final String MODIFY_INDICATOR = "modify_indicator";
	
	/**
	 * Loaded content packs.
	 */
	public static final Registry< IContentProvider > CONTENT_PROVIDERS = new Registry<>();
	
	/**
	 * @see IAutowirePacketHandler
	 */
	public static final PacketHandler NET = new PacketHandler( MODID );
	
	/**
	 * @see IAutowireLogger
	 */
	public static final Logger LOGGER = LogManager.getLogger( MODID );
	
	/**
	 * Localized config from {@link ModConfig#maxSlotCapacity}.
	 */
	public static int maxSlotCapacity;
	
	/**
	 * A reference to ".minecraft/" folder.
	 */
	protected final File gameDir = Loader.instance().getConfigDir().getParentFile();
	
	/**
	 * Loaded sound pool.
	 */
	final HashMap< String, SoundEvent > soundPool = new HashMap<>();
	
	/**
	 * Registered subscribers for post load callback.
	 */
	private final LinkedList< IPostLoadSubscriber > postLoadSubscribers = new LinkedList<>();
	
//	private final HashMap< String, byte[] > classes = new HashMap<>();
	
	protected FMUM() { super( new URL[ 0 ], MinecraftServer.class.getClassLoader() ); }
	
	@EventHandler
	public final void onPreInit( FMLPreInitializationEvent evt )
	{
		// Info load start.
		this.logInfo( "fmum.on_pre_init" );
		
		// Prepare pack load.
		this.preLoad();
		
		// Load content packs.
		this.load();
		
		this.logInfo( "fmum.pre_init_complete" );
	}
	
	@EventHandler
	public final void onInit( FMLInitializationEvent evt )
	{
		this.logInfo( "fmum.on_init" );
		
		// Fire load callback.
		this.postLoadSubscribers.forEach( IPostLoadSubscriber::onPostLoad );
		this.postLoadSubscribers.clear();
		
		NET.regisPackets();
		
		this.logInfo( "fmum.init_complete" );
		
		// After initialization, info user all the packs being loaded.
		this.logInfo( "fmum.total_loaded_packs", "" + CONTENT_PROVIDERS.size() );
		CONTENT_PROVIDERS.values().forEach( pack -> {
			final String packName = this.format( pack.name() );
			final String packAuthor = this.format( pack.author() );
			this.logInfo( "fmum.info_loaded_pack", packName, packAuthor );
		} );
	}
	
	@EventHandler
	public final void onPostInit( FMLPostInitializationEvent evt )
	{
		this.logInfo( "fmum.on_post_init" );
		
		// TODO: Whether to support packets registration for packs?
//		NET.postInit();
		
		this.logInfo( "fmum.post_init_complete" );
	}
	
	@Override
	public void preLoad()
	{
		// Register capabilities.
		this.regisCapability( IItem.class ); // See ItemType#CONTEXTED.
		this.regisCapability( PlayerPatch.class );
		
		// Register meta loaders.
		TYPE_LOADERS.put( "creative_tab", CreativeTab.LOADER );
		TYPE_LOADERS.put( "creative_tabs", CreativeTab.LOADER );
		TYPE_LOADERS.put( "gun_part", JsonGunPartType.LOADER );
		TYPE_LOADERS.put( "gun_parts", JsonGunPartType.LOADER );
		TYPE_LOADERS.put( "gun", JsonGunType.LOADER );
		TYPE_LOADERS.put( "guns", JsonGunType.LOADER );
		TYPE_LOADERS.put( "mag", JsonMagType.LOADER );
		TYPE_LOADERS.put( "mags", JsonMagType.LOADER );
		TYPE_LOADERS.put( "ammo", JsonAmmoType.LOADER );
		TYPE_LOADERS.put( "paintjob", JsonPaintjob.LOADER );
		TYPE_LOADERS.put( "paintjobs", JsonPaintjob.LOADER );
		this.regisSideDependentLoaders();
	}
	
	@Override
	public void load()
	{
		// Construct a default indicator.
		// Has to put it here as current implementation will create an item for it.
		final JsonObject indicator = new JsonObject();
		indicator.addProperty( "creativeTab", FMUM.HIDE_TAB.name() );
		indicator.addProperty( "model", "models/modify_indicator.json" );
		indicator.addProperty( "texture", "textures/0x00ff00.png" );
		TYPE_LOADERS.get( "gun_part" ).parser.apply( indicator ).build( MODIFY_INDICATOR, this );
		
		// Check content pack folder.
		// TODO: if load packs from mods dir then allow the player to disable content pack folder
		final File packDir = new File( this.gameDir, MODID );
		if ( !packDir.exists() )
		{
			packDir.mkdirs();
			this.logInfo( "fmum.pack_dir_created", MODID );
		}
		
		// Compile a regex to match the supported content pack file types.
		// TODO: check if zip is supported
		final Pattern jarRegex = Pattern.compile( "(.+)\\.(zip|jar)$" );
		
		// Find all content packs and load them.
		final HashSet< IContentProvider > providers = new HashSet<>();
		final Consumer< IContentProvider > addPack = pack -> {
			providers.add( pack );
			this.logInfo( "fmum.detect_content_pack", pack.sourceName() );
		};
		for ( final File file : packDir.listFiles() )
		{
			final boolean isFolderPack = file.isDirectory();
			if ( isFolderPack )
			{
				addPack.accept( new FolderPack( file ) );
				continue;
			}
			
			final boolean isJarPack = jarRegex.matcher( file.getName() ).matches();
			if ( isJarPack )
			{
				addPack.accept( new JarPack( file ) );
				continue;
			}
			
			final String filePath = packDir.getName() + "/" + file.getName();
			this.logError( "fmum.unknown_pack_file_type", filePath );
		}
		
		// TODO: Post provider registry event to get providers from other mods?
//		MinecraftForge.EVENT_BUS.post( new ContentProviderRegistryEvent( providers ) );
		
		// Let content packs register resource domains and reload Minecraft resources.
		providers.forEach( IContentProvider::preLoad );
		this.reloadResources(); // TODO: check if it works without this
		
		// Load content packs!
		providers.forEach( pack -> {
			this.logInfo( "fmum.load_content_pack", pack.sourceName() );
			pack.load();
			CONTENT_PROVIDERS.regis( pack );
		} );
	}
	
	public void addResourceDomain( File file )
	{
		try { this.addURL( file.toURI().toURL() ); }
		catch ( MalformedURLException e ) {
			this.logException( e, "fmum.error_adding_classpath", file.getName() );
		}
	}
	
	public final < T > void regisCapability( Class< T > capabilityClass )
	{
		final IStorage< T > defaultSerializer = new Capability.IStorage< T >()
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
		};
		final Callable< T > defaultFactory = () -> null;
		CapabilityManager.INSTANCE.register( capabilityClass, defaultSerializer, defaultFactory );
	}
	
	@Override
	public void regisPostLoadSubscriber( IPostLoadSubscriber subscriber ) {
		this.postLoadSubscribers.add( subscriber );
	}
	
	@Override
	public void regisMeshLoadSubscriber( IMeshLoadSubscriber subscriber ) { }
	
	@Override
	public SoundEvent loadSound( String path )
	{
		return this.soundPool.computeIfAbsent( path, key -> {
			final FMUMResource res = new FMUMResource( path );
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
	public String name() { return "fmum.core_pack"; }
	
	@Override
	public String author() { return "fmum.author"; }
	
	@Override
	public String sourceName() { return MOD_NAME; }
	
	@Override
	public String toString() { return MODID; }
	
	protected void regisSideDependentLoaders()
	{
		// Key binds is client only. But to avoid the errors when it is absent, set a loader that \
		// simply does nothing on load.
		final BuildableLoader< IMeta > keyBindLoader = new BuildableLoader<>(
			"key_binding", json -> ( name, provider ) -> null
		);
		TYPE_LOADERS.put( "key_binding", keyBindLoader );
		TYPE_LOADERS.put( "key_bindings", keyBindLoader );
	}
	
	/**
	 * Client only to reload resources.
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
		builder.registerTypeAdapter( IOperationController.class, OperationController.ADAPTER );
		
		final JsonDeserializer< ModuleCategory > moduleCategoryAdapter =
			( json, typeOfT, context ) -> new ModuleCategory( json.getAsString() );
		builder.registerTypeAdapter( ModuleCategory.class, moduleCategoryAdapter );
		
		final JsonDeserializer< ModuleFilter > moduleFilterAdapter =
			( json, typeOfT, context ) -> new ModuleFilter( json );
		builder.registerTypeAdapter( ModuleFilter.class, moduleFilterAdapter );
		
		final JsonDeserializer< SoundEvent > soundAdapter =
			( json, typeOfT, context ) -> this.loadSound( json.getAsString() );
		builder.registerTypeAdapter( SoundEvent.class, soundAdapter );
		
		final JsonDeserializer< Vec3f > vecAdapter = ( json, typeOfT, context ) -> {
			final JsonArray arr = json.getAsJsonArray();
			return new Vec3f(
				arr.get( 0 ).getAsFloat(),
				arr.get( 1 ).getAsFloat(),
				arr.get( 2 ).getAsFloat()
			);
		};
		builder.registerTypeAdapter( Vec3f.class, vecAdapter );
		
		final JsonDeserializer< AngleAxis4f > angleAxisAdapter = ( json, typeOfT, context ) -> {
			final JsonArray arr = json.getAsJsonArray();
			final float f0 = arr.get( 0 ).getAsFloat();
			final float f1 = arr.get( 1 ).getAsFloat();
			final float f2 = arr.get( 2 ).getAsFloat();
			return (
				arr.size() < 4
				? new AngleAxis4f( f0, f1, f2 )
				: new AngleAxis4f( f0, f1, f2, arr.get( 3 ).getAsFloat() )
			);
		};
		builder.registerTypeAdapter( AngleAxis4f.class, angleAxisAdapter );
		
		final JsonDeserializer< Quat4f > quatAdapter = ( json, typeOfT, context ) -> 
			new Quat4f( angleAxisAdapter.deserialize( json, typeOfT, context ) );
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
