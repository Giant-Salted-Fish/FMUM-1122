package com.fmum.common;

import com.fmum.client.FMUMClient;
import com.fmum.common.ammo.JsonAmmoType;
import com.fmum.common.gun.ControllerDispatcher;
import com.fmum.common.gun.IFireController;
import com.fmum.common.gun.IFireController.RPMController;
import com.fmum.common.gun.JsonGunPartType;
import com.fmum.common.gun.JsonGunType;
import com.fmum.common.item.IItem;
import com.fmum.common.item.IItemType;
import com.fmum.common.load.BuildableLoader;
import com.fmum.common.load.IContentProvider;
import com.fmum.common.load.IMeshLoadSubscriber;
import com.fmum.common.load.IPostLoadSubscriber;
import com.fmum.common.mag.JsonMagType;
import com.fmum.common.meta.IMeta;
import com.fmum.common.meta.MetaRegistry;
import com.fmum.common.module.IModuleSlot;
import com.fmum.common.module.ModuleCategory;
import com.fmum.common.module.ModuleFilter;
import com.fmum.common.module.RailSlot;
import com.fmum.common.network.IPacket;
import com.fmum.common.network.PacketHandler;
import com.fmum.common.pack.FolderPack;
import com.fmum.common.pack.JarPack;
import com.fmum.common.paintjob.IPaintjob;
import com.fmum.common.paintjob.JsonPaintjob;
import com.fmum.common.paintjob.Paintjob;
import com.fmum.common.player.OperationController.TimedEffect;
import com.fmum.common.player.OperationController.TimedSound;
import com.fmum.common.player.PlayerPatch;
import com.fmum.common.tab.CreativeTab;
import com.fmum.util.AngleAxis4f;
import com.fmum.util.Quat4f;
import com.fmum.util.Vec3f;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayerMP;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

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
public class FMUM extends URLClassLoader implements IContentProvider
{
	public static final String MODID = "fmum";
	
	public static final String MOD_NAME = "FMUM 2.0";
	
	public static final String MOD_VERSION = "0.3.3-alpha";
	
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
	 * Default json parser to load types in content pack. Only available after
	 * {@link IContentProvider#preLoad(BiConsumer)} call.
	 */
	public static Gson GSON;
	
	/**
	 * Registered type loaders.
	 */
	public static final Registry< BuildableLoader< ? extends IMeta > >
		TYPE_LOADERS = new Registry<>();
	
	public static final Registry<
		BiFunction< Entry< String, JsonElement >, JsonDeserializationContext, IFireController >
	> FIRE_CONTROLLER_LOADERS = new Registry<>();
	
	/**
	 * Default creative item tab for {@link FMUM}.
	 */
	public static final CreativeTab DEFAULT_TAB = new CreativeTab()
	{
		// This helps to ensure the default tab will only initialize one time when on the first \
		// item settle in.
		Runnable initializer = () -> {
			this.build( MODID, MOD );
			this.initializer = () -> { };
		};
		
		@Override
		public void itemSettledIn( IItemType item )
		{
			this.initializer.run();
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
	public static final MetaRegistry< IContentProvider > CONTENT_PROVIDERS = new MetaRegistry<>();
	
	/**
	 * Use this to send network packets.
	 */
	protected static final PacketHandler NET = new PacketHandler( MODID );
	
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
		logInfo( "fmum.on_pre_init" );
		
		// Load content packs.
		this.load();
		
		logInfo( "fmum.pre_init_complete" );
	}
	
	@EventHandler
	public final void onInit( FMLInitializationEvent evt )
	{
		logInfo( "fmum.on_init" );
		
		// Fire load callback.
		this.postLoadSubscribers.forEach( IPostLoadSubscriber::onPostLoad );
		this.postLoadSubscribers.clear();
		
		NET.regisPackets();
		
		logInfo( "fmum.init_complete" );
		
		// After initialization, info user all the packs being loaded.
		logInfo( "fmum.total_loaded_packs", String.valueOf( CONTENT_PROVIDERS.size() ) );
		CONTENT_PROVIDERS.values().forEach( pack -> {
			final String packName = this.format( pack.name() );
			final String packAuthor = this.format( pack.author() );
			logInfo( "fmum.info_loaded_pack", packName, packAuthor );
		} );
	}
	
	@EventHandler
	public final void onPostInit( FMLPostInitializationEvent evt )
	{
		logInfo( "fmum.on_post_init" );
		
		// TODO: Whether to support packets registration for packs?
//		NET.postInit();
		
		logInfo( "fmum.post_init_complete" );
	}
	
	@Override
	public void load()
	{
		// Check content pack folder.
		// TODO: if load packs from mods dir then allow the player to disable content pack folder
		final File packDir = new File( this.gameDir, MODID );
		if ( !packDir.exists() )
		{
			packDir.mkdirs();
			logInfo( "fmum.pack_dir_created", MODID );
		}
		
		// Compile a regex to match the supported content pack file types.
		// TODO: check if zip is supported
		final Pattern jarRegex = Pattern.compile( "(.+)\\.(zip|jar)$" );
		
		// Find all content packs and load them.
		final HashSet< IContentProvider > providers = new HashSet<>();
		final Consumer< IContentProvider > addPack = pack -> {
			providers.add( pack );
			logInfo( "fmum.detect_content_pack", pack.sourceName() );
		};
		for ( File file : packDir.listFiles() )
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
			logError( "fmum.unknown_pack_file_type", filePath );
		}
		
		// TODO: Post provider registry event to get providers from other mods?
//		MinecraftForge.EVENT_BUS.post( new ContentProviderRegistryEvent( providers ) );
		
		// Call prepare load for all content packs and build up Json parser.
		final GsonBuilder builder = new GsonBuilder();
		builder.setLenient();
		builder.setPrettyPrinting();
		
		this.preLoad( builder::registerTypeAdapter );
		providers.forEach( provider -> provider.preLoad( builder::registerTypeAdapter ) );
		
		this.reloadResources();
		GSON = builder.create();
		
		// Construct a default indicator.
		// Has to put it before the item registry as current implementation will create an \
		// corresponding item for it.
		final JsonObject indicator = new JsonObject();
		indicator.addProperty( "creativeTab", HIDE_TAB.name() );
		indicator.addProperty( "model", "models/modify_indicator.json" );
		indicator.addProperty( "texture", "textures/0x00ff00.png" );
		JsonGunPartType.LOADER.parser.apply( indicator ).build( MODIFY_INDICATOR, this );
		
		// Load content packs!
		providers.forEach( pack -> {
			logInfo( "fmum.load_content_pack", pack.sourceName() );
			pack.load();
			CONTENT_PROVIDERS.regis( pack );
		} );
	}
	
	/**
	 * Called in {@link #load()} to prepare content pack load.
	 */
	@Override
	public void preLoad( BiConsumer< Type, JsonDeserializer< ? > > gsonAdapterRegis )
	{
		// Register gson adapters.
		gsonAdapterRegis.accept(
			IModuleSlot.class,
			( json, typeOfT, context ) -> context.deserialize( json, RailSlot.class )
		);
		gsonAdapterRegis.accept(
			IPaintjob.class,
			( json, typeOfT, context ) -> context.deserialize( json, Paintjob.class )
		);
		gsonAdapterRegis.accept(
			ControllerDispatcher.class,
			( json, typeOfT, context ) -> new ControllerDispatcher( json, context )
		);
		gsonAdapterRegis.accept(
			TimedSound[].class,
			new ArrJsonDeserializer<>(
				TimedSound[]::new,
				( entry, context ) -> new TimedSound(
					Float.parseFloat( entry.getKey() ),
					entry.getValue().getAsString()
				)
			)
		);
		gsonAdapterRegis.accept(
			TimedEffect[].class,
			new ArrJsonDeserializer<>(
				TimedEffect[]::new,
				( entry, context ) -> new TimedEffect(
					Float.parseFloat( entry.getKey() ),
					entry.getValue().getAsString()
				)
			)
		);
		gsonAdapterRegis.accept(
			IFireController[].class,
			new ArrJsonDeserializer<>(
				IFireController[]::new,
				( entry, context ) -> {
					final String type = entry.getKey();
					final BiFunction<
						Entry< String, JsonElement >, JsonDeserializationContext, IFireController
					> loader = FIRE_CONTROLLER_LOADERS.get( type );
					if ( loader != null ) { return loader.apply( entry, context ); }
					
					logError( "fmum.fire_controller_loader_not_found", type );
					return IFireController.SAFETY;
				}
			)
		);
		gsonAdapterRegis.accept(
			ModuleCategory.class,
			( json, typeOfT, context ) -> new ModuleCategory( json.getAsString() )
		);
		gsonAdapterRegis.accept(
			ModuleFilter.class,
			( json, typeOfT, context ) -> new ModuleFilter( json )
		);
		gsonAdapterRegis.accept(
			SoundEvent.class,
			( json, typeOfT, context ) -> this.loadSound( json.getAsString() )
		);
		gsonAdapterRegis.accept(
			Vec3f.class,
			( json, typeOfT, context ) -> {
				final JsonArray arr = json.getAsJsonArray();
				return new Vec3f(
					arr.get( 0 ).getAsFloat(),
					arr.get( 1 ).getAsFloat(),
					arr.get( 2 ).getAsFloat()
				);
			}
		);
		
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
		gsonAdapterRegis.accept( AngleAxis4f.class, angleAxisAdapter );
		
		gsonAdapterRegis.accept(
			Quat4f.class,
			( json, typeOfT, context ) ->
				new Quat4f( angleAxisAdapter.deserialize( json, typeOfT, context ) )
		);
		
		// Register capabilities.
		this.regisCapability( IItem.class ); // See ItemType#CONTEXTED.
		this.regisCapability( PlayerPatch.class );
		
		// Register type loaders.
		TYPE_LOADERS.regis( "creative_tab", CreativeTab.LOADER );
		TYPE_LOADERS.regis( "creative_tabs", CreativeTab.LOADER );
		TYPE_LOADERS.regis( "gun_part", JsonGunPartType.LOADER );
		TYPE_LOADERS.regis( "gun_parts", JsonGunPartType.LOADER );
		TYPE_LOADERS.regis( "attachment", JsonGunPartType.LOADER );
		TYPE_LOADERS.regis( "attachments", JsonGunPartType.LOADER );
		TYPE_LOADERS.regis( "gun", JsonGunType.LOADER );
		TYPE_LOADERS.regis( "guns", JsonGunType.LOADER );
		TYPE_LOADERS.regis( "mag", JsonMagType.LOADER );
		TYPE_LOADERS.regis( "mags", JsonMagType.LOADER );
		TYPE_LOADERS.regis( "ammo", JsonAmmoType.LOADER );
		TYPE_LOADERS.regis( "paintjob", JsonPaintjob.LOADER );
		TYPE_LOADERS.regis( "paintjobs", JsonPaintjob.LOADER );
		this.regisSideDependentLoaders();
		
		// Register fire controller loaders.
		FIRE_CONTROLLER_LOADERS.regis( "rpm_based", RPMController::new );
		FIRE_CONTROLLER_LOADERS.regis( "full_auto", RPMController::new );
		FIRE_CONTROLLER_LOADERS.regis( "semi_auto", ( entry, context ) -> {
			final JsonObject obj = ( JsonObject ) entry.getValue();
			obj.addProperty( "actionRounds", 1 );
			return new RPMController( entry, context );
		} );
		FIRE_CONTROLLER_LOADERS.regis( "burst", RPMController::new );
		FIRE_CONTROLLER_LOADERS.regis( "safety", ( jsonElement, context ) -> IFireController.SAFETY );
	}
	
	public void addResourceDomain( File file )
	{
		try { this.addURL( file.toURI().toURL() ); }
		catch ( MalformedURLException e ) {
			logException( e, "fmum.error_adding_classpath", file.getName() );
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
	public final void regisPostLoadSubscriber( IPostLoadSubscriber subscriber ) {
		this.postLoadSubscribers.add( subscriber );
	}
	
	@Override
	public void regisMeshLoadSubscriber( IMeshLoadSubscriber subscriber ) { }
	
	@Override
	public final SoundEvent loadSound( String path )
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
	
	@SuppressWarnings( "deprecation" )
	public String format( String translateKey, Object... parameters )
	{
		return net.minecraft.util.text.translation
			.I18n.translateToLocalFormatted( translateKey, parameters );
	}
	
	@Override
	public final String name() { return "fmum.core_pack"; }
	
	@Override
	public final String author() { return "fmum.author"; }
	
	@Override
	public final String sourceName() { return MOD_NAME; }
	
	@Override
	public final String toString() { return MODID; }
	
	protected void regisSideDependentLoaders()
	{
		// Key binds is client only. But to avoid the errors when it is absent, set a loader that \
		// simply does nothing on load.
		final BuildableLoader< IMeta > keyBindLoader = new BuildableLoader<>(
			"key_binding", json -> ( name, provider ) -> null
		);
		TYPE_LOADERS.regis( "key_binding", keyBindLoader );
		TYPE_LOADERS.regis( "key_bindings", keyBindLoader );
	}
	
	/**
	 * Client only to reload resources.
	 */
	protected void reloadResources() { }
	
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
	
	public static void sendPacketTo( IPacket packet, EntityPlayerMP player ) {
		NET.sendTo( packet, player );
	}
	
	public static void logInfo( String translateKey, Object... parameters ) {
		LOGGER.info( MOD.format( translateKey, parameters ) );
	}
	
	public static void logWarning( String translateKey, Object... parameters ) {
		LOGGER.warn( MOD.format( translateKey, parameters ) );
	}
	
	public static void logError( String translateKey, Object... parameters ) {
		LOGGER.error( MOD.format( translateKey, parameters ) );
	}
	
	public static void logException( Throwable e, String translateKey, Object... parameters ) {
		LOGGER.error( MOD.format( translateKey, parameters ), e );
	}
	
	public static final class ArrJsonDeserializer< T > implements JsonDeserializer< T[] >
	{
		private final Function< Integer, T[] > arrFactory;
		private final BiFunction< Entry< String, JsonElement >, JsonDeserializationContext, T >
			parser;
		
		public ArrJsonDeserializer(
			Function< Integer, T[] > arrFactory,
			BiFunction< Entry< String, JsonElement >, JsonDeserializationContext, T > parser
		) {
			this.arrFactory = arrFactory;
			this.parser = parser;
		}
		
		@Override
		public T[] deserialize( JsonElement json, Type typeOfT, JsonDeserializationContext context )
		{
			final JsonObject obj = ( JsonObject ) json;
			final T[] arr = this.arrFactory.apply( obj.size() );
			obj.entrySet().forEach( new Consumer< Entry< String, JsonElement > >() {
				private int i = 0;
				
				@Override
				public void accept( Entry< String, JsonElement > entry )
				{
					arr[ this.i ] = ArrJsonDeserializer.this.parser.apply( entry, context );
					this.i += 1;
				}
			} );
			return arr;
		}
	}
}
