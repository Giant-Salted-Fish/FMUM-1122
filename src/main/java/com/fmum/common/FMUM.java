package com.fmum.common;

import com.fmum.client.FMUMClient;
import com.fmum.common.item.IItem;
import com.fmum.common.network.IPacket;
import com.fmum.common.network.PacketHandler;
import com.fmum.common.pack.FolderPack;
import com.fmum.common.pack.IContentPack;
import com.fmum.common.pack.ILoadablePack;
import com.fmum.common.pack.ILoadablePack.IBuildContext;
import com.fmum.common.pack.ILoadablePack.IContentLoader;
import com.fmum.common.pack.ILoadablePack.ILoadContext;
import com.fmum.common.pack.ILoadablePack.IPrepareContext;
import com.fmum.common.pack.JarPack;
import com.fmum.common.player.PlayerPatch;
import com.fmum.common.tab.CreativeTab;
import com.fmum.common.tab.ICreativeTab;
import com.fmum.util.AngleAxis4f;
import com.fmum.util.Quat4f;
import com.fmum.util.Vec3f;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
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
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * {@link FMUM} provides a platform to load highly customizable weapons.
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
public class FMUM implements IContentPack
{
	public static final String MODID = "fmum";
	public static final String MOD_NAME = "FMUM 2.0";
	public static final String MOD_VERSION = "0.3.3-alpha";
	
	public static final FMUM MOD;
	static
	{
		final Supplier< ? > client_mod = () -> FMUMClient.MOD; // Use generic will crash.
		final Side side = FMLCommonHandler.instance().getSide();
		MOD = side.isServer() ? new FMUM() : ( FMUM ) client_mod.get();
	}
	@Mod.InstanceFactory
	public static FMUM instance() { return MOD; }
	
	/**
	 * You can use this to customized log if none of the util log function provided in this class
	 * fits your case.
	 *
	 * @see #logInfo(String, Object...)
	 * @see #logWarning(String, Object...)
	 * @see #logError(String, Object...)
	 * @see #logException(Throwable, String, Object...)
	 */
	public final static Logger LOGGER = LogManager.getLogger( MODID );
	
	public static final ICreativeTab DEFAULT_CREATIVE_TAB = new CreativeTab()
	{
		private Runnable initializer = () -> {
			final IBuildContext ctx = new IBuildContext()
			{
				@Override
				public String fallbackName() {
					return MODID;
				}
				
				@Override
				public IContentPack contentPack() {
					return MOD;
				}
				
				@Override
				public Gson gson() {
					throw new RuntimeException();
				}
				
				@Override
				public void regisPostLoadCallback( Runnable callback ) {
					throw new RuntimeException();
				}
			};
			
			if ( MOD.isClient() ) {
				this.buildClientSide( ctx );
			}
			else {
				this.buildServerSide( ctx );
			}
			
			this.initializer = () -> { };
		};
	};
	
	protected static final PacketHandler PACKET_HANDLER = new PacketHandler( MODID );
	
	
	public final Registry< IContentPack > content_packs = new Registry<>( IContentPack::name );
	
	protected final File game_dir = Loader.instance().getConfigDir().getParentFile();
	
	private final LinkedList< Supplier< IContentPack > > unfinalized_packs = new LinkedList<>();
	
	// TODO: This may also be temporary?
	private final FMUMClassLoader class_loader = new FMUMClassLoader();
	
	protected FMUM() { }
	
	@Mod.EventHandler
	public final void onPreInit( FMLPreInitializationEvent evt )
	{
		logInfo( "fmum.on_pre_init" );
		
		this._loadContentPacks();
		
		logInfo( "fmum.pre_init_complete" );
	}
	
	@Mod.EventHandler
	public final void onInit( FMLInitializationEvent evt )
	{
		logInfo( "fmum.on_init" );
		
		this.__finalizePacksAndPacketHandler();
		
		logInfo( "fmum.init_complete" );
		
		this.__printAllLoadedPacks();
	}
	
	@Mod.EventHandler
	public final void onPostInit( FMLPostInitializationEvent evt )
	{
		logInfo( "fmum.on_post_init" );
		
		// TODO: Whether to support packets registration for packs?
//		PACKET_HANDLER.postInit();
		
		logInfo( "fmum.post_init_complete" );
	}
	
	protected void _loadContentPacks()
	{
		final Iterable< ILoadablePack > loadable_packs = this.__gatherLoadablePacks();
		
		// Prepare JSON parser and type loader.
		final GsonBuilder gson_builder = new GsonBuilder();
		gson_builder.setLenient();
		gson_builder.setPrettyPrinting();
		
		Registry< IContentLoader > content_loaders = new Registry<>();
		
		// Call prepare load for each pack.
		final LinkedList< Function< ILoadContext, Supplier< IContentPack > > >
			pack_loaders = new LinkedList<>();
		final IPrepareContext prepare_context = new IPrepareContext()
		{
			@Override
			public void regisPackLoader(
				Function< ILoadContext, Supplier< IContentPack > > pack_loader
			) { pack_loaders.add( pack_loader ); }
			
			@Override
			public void regisGsonAdapter( Type type, JsonDeserializer< ? > adapter ) {
				gson_builder.registerTypeAdapter( type, adapter );
			}
			
			@Override
			public void regisContentLoader( String entry, IContentLoader loader ) {
				content_loaders.regis( entry, loader );
			}
			
			@Override
			public void regisResourceDomain( File file ) {
				FMUM.this._regisResourceDomain( file );
			}
			
			@Override
			public < T > void regisCapability( Class< T > capability_class )
			{
				final IStorage< T > defaultSerializer = new Capability.IStorage< T >()
				{
					@Nullable
					@Override
					public NBTBase writeNBT(
						Capability< T > capability,
						T instance,
						EnumFacing side
					) { return null; }
					
					@Override
					public void readNBT(
						Capability< T > capability,
						T instance,
						EnumFacing side,
						NBTBase nbt
					) { }
				};
				final Callable< T > default_factory = () -> null;
				CapabilityManager.INSTANCE.register(
					capability_class, defaultSerializer, default_factory );
			}
		};
		this.__regisCapability( prepare_context );
		this._regisGsonAdapter( prepare_context );
		this._regisContentLoader( prepare_context );
		loadable_packs.forEach( this._callPrepareLoadPack( prepare_context ) );
		
		final Gson gson = gson_builder.create();
		this._reloadResources();
		
		// Load content packs!
		final ILoadContext load_context = new ILoadContext()
		{
			@Override
			public Gson gson() {
				return gson;
			}
			
			@Override
			public Optional< IContentLoader > getContentLoader( String entry ) {
				return Optional.ofNullable( content_loaders.get( entry ) );
			}
		};
		pack_loaders.forEach( loader -> {
			final Supplier< IContentPack > loaded_pack = loader.apply( load_context );
			this.unfinalized_packs.add( loaded_pack );
		} );
	}
	
	private Iterable< ILoadablePack > __gatherLoadablePacks()
	{
		final File pack_dir = new File( this.game_dir, MODID );
		if ( !pack_dir.exists() )
		{
			pack_dir.mkdirs();
			logInfo( "fmum.pack_dir_created", MODID );
		}
		
		final LinkedList< ILoadablePack > loadable_packs = new LinkedList<>();
		final Pattern jarRegex = Pattern.compile( "(.+)\\.(zip|jar)$" );
		
		for ( final File file : pack_dir.listFiles() )
		{
			// TODO: if load packs from mods dir then allow the player to disable content pack folder.
			final boolean is_folder_pack = file.isDirectory();
			if ( is_folder_pack )
			{
				loadable_packs.add( new FolderPack( file ) );
				logInfo( "fmum.detect_content_pack", file.getName() );
				continue;
			}
			
			final boolean is_jar_pack = jarRegex.matcher( file.getName() ).matches();
			if ( is_jar_pack )
			{
				loadable_packs.add( new JarPack( file ) );
				logInfo( "fmum.detect_content_pack", file.getName() );
				continue;
			}
			
			final String file_path = pack_dir.getName() + "/" + file.getName();
			logError( "fmum.unknown_pack_file_type", file_path );
		}
		
		// TODO: Post provider registry event to get providers from other mods?
//		MinecraftForge.EVENT_BUS.post( new GatherLoadablePackEvent( loadable_packs::add ) );
		return loadable_packs;
	}
	
	protected void _regisResourceDomain( File file )
	{
		try
		{
			final URL path_url = file.toURI().toURL();
			FMUM.this.class_loader.addURL( path_url );
		}
		catch ( MalformedURLException e ) {
			logException( e, "fmum.error_adding_classpath", file.getName() );
		}
	}
	
	private void __regisCapability( IPrepareContext ctx )
	{
		ctx.regisCapability( IItem.class ); // See ItemType#CONTEXTED.
		ctx.regisCapability( PlayerPatch.class );
	}
	
	protected void _regisGsonAdapter(IPrepareContext ctx )
	{
		ctx.regisGsonAdapter(
			Vec3f.class,
			( json, type_of_T, context ) -> {
				final JsonArray arr = json.getAsJsonArray();
				return new Vec3f(
						arr.get( 0 ).getAsFloat(),
						arr.get( 1 ).getAsFloat(),
						arr.get( 2 ).getAsFloat()
				);
			}
		);
		
		ctx.regisGsonAdapter(
			AngleAxis4f.class,
			( json, type_of_T, context ) -> {
				final JsonArray arr = json.getAsJsonArray();
				final float f0 = arr.get( 0 ).getAsFloat();
				final float f1 = arr.get( 1 ).getAsFloat();
				final float f2 = arr.get( 2 ).getAsFloat();
				return (
						arr.size() < 4
								? new AngleAxis4f( f0, f1, f2 )
								: new AngleAxis4f( f0, f1, f2, arr.get( 3 ).getAsFloat() )
				);
			}
		);
		
		ctx.regisGsonAdapter(
			Quat4f.class,
			( json, type_of_T, context ) -> {
				final AngleAxis4f rot = context.deserialize( json, AngleAxis4f.class );
				return new Quat4f( rot );
			}
		);
	}
	
	protected void _regisContentLoader( IPrepareContext ctx )
	{
		ctx.regisContentLoader( "creative_tab", CreativeTab.class, CreativeTab::buildServerSide );
	}
	
	protected Consumer< ILoadablePack > _callPrepareLoadPack(IPrepareContext ctx ) {
		return pack -> pack.prepareLoadServerSide( ctx );
	}
	
	protected void _reloadResources() { }
	
	private void __finalizePacksAndPacketHandler()
	{
		this.unfinalized_packs.forEach( unfinalized_pack -> {
			final IContentPack pack = unfinalized_pack.get();
			this.content_packs.regis( pack );
		} );
		this.unfinalized_packs.clear();
		
		PACKET_HANDLER.regisPackets();
	}
	
	private void __printAllLoadedPacks()
	{
		logInfo( "fmum.total_loaded_packs", String.valueOf( this.content_packs.size() ) );
		this.content_packs.values().forEach( pack -> {
			final String pack_name = this.format( pack.name() );
			final String pack_author = this.format( pack.author() );
			logInfo( "fmum.info_loaded_pack", pack_name, pack_author );
		} );
	}
	
	@Override
	public final String name() {
		return "fmum.pack";
	}
	
	@Override
	public final String author() {
		return "fmum.author";
	}
	
	@Override
	public final String sourceName() {
		return MOD_NAME;
	}
	
	/**
	 * Use this to do localization if your code runs on both side to avoid crash.
	 */
	@SuppressWarnings( "deprecation" )
	public String format( String translate_key, Object... parameters )
	{
		return net.minecraft.util.text.translation
			.I18n.translateToLocalFormatted( translate_key, parameters );
	}
	
	public static void logInfo( String translate_key, Object... parameters ) {
		LOGGER.info( MOD.format( translate_key, parameters ) );
	}
	
	public static void logWarning( String translate_key, Object... parameters ) {
		LOGGER.warn( MOD.format( translate_key, parameters ) );
	}
	
	public static void logError( String translate_key, Object... parameters ) {
		LOGGER.error( MOD.format( translate_key, parameters ) );
	}
	
	public static void logException( Throwable e, String translate_key, Object... parameters ) {
		LOGGER.error( MOD.format( translate_key, parameters ), e );
	}
	
	public static void sendToPlayer( IPacket packet, EntityPlayerMP player ) {
		PACKET_HANDLER.sendTo( packet, player );
	}
	
	public boolean isClient() {
		return false;
	}
	
	
	private static final class FMUMClassLoader extends URLClassLoader
	{
		private FMUMClassLoader() {
			super( new URL[ 0 ], MinecraftServer.class.getClassLoader() );
		}
		
		@Override
		protected void addURL( URL url ) {
			super.addURL( url );
		}
	}
}
