package com.fmum.common;

import com.fmum.client.FMUMClient;
import com.fmum.common.network.IPacket;
import com.fmum.common.network.PacketHandler;
import com.fmum.common.pack.FolderPack;
import com.fmum.common.pack.IContentPack;
import com.fmum.common.pack.IContentPack.ILoadContext;
import com.fmum.common.pack.IContentPack.IPrepareContext;
import com.fmum.common.pack.JarPack;
import com.fmum.common.player.PlayerPatch;
import com.fmum.util.AngleAxis4f;
import com.fmum.util.Quat4f;
import com.fmum.util.Vec3f;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
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
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * {@link FMUM} provides a platform to load highly customizable weapons.
 *
 * @author Giant_Salted_Fish
 */
@Mod(
	modid = FMUM.MOD_ID,
	name = FMUM.MOD_NAME,
	version = FMUM.MOD_VERSION,
	acceptedMinecraftVersions = "[1.12, 1.13)",
	guiFactory = "com.fmum.client.ConfigGuiFactory"
//	, clientSideOnly = true
)
public class FMUM
{
	public static final String MOD_ID = "fmum";
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
	public final static Logger LOGGER = LogManager.getLogger( MOD_ID );
	
	protected static final PacketHandler PACKET_HANDLER = new PacketHandler( MOD_ID );
	
	
	public final Registry< IContentPack > loaded_packs = new Registry<>( IContentPack::name );
	
	protected final File game_dir = Loader.instance().getConfigDir().getParentFile();
	
	// TODO: This may also be temporary?
	private final FMUMClassLoader class_loader = new FMUMClassLoader();
	
	protected FMUM() { }
	
	@Mod.EventHandler
	public final void onPreInit( FMLPreInitializationEvent evt )
	{
		// Info load start.
		logInfo( "fmum.on_pre_init" );
		
		// Load content packs.
		this._loadContentPacks();
		
		logInfo( "fmum.pre_init_complete" );
	}
	
	protected void _loadContentPacks()
	{
		// Check content pack folder.
		// TODO: if load packs from mods dir then allow the player to disable content pack folder.
		final File pack_dir = new File( this.game_dir, MOD_ID );
		if ( !pack_dir.exists() )
		{
			pack_dir.mkdirs();
			logInfo( "fmum.pack_dir_created", MOD_ID );
		}
		
		// Find possible content packs to load.
		final HashSet< IContentPack > content_packs = new HashSet<>();
		final Pattern jarRegex = Pattern.compile( "(.+)\\.(zip|jar)$" );
		final Consumer< IContentPack > add_pack_and_log = pack -> {
			content_packs.add( pack );
			logInfo( "fmum.detect_content_pack", pack.sourceName() );
		};
		
		for ( final File file : pack_dir.listFiles() )
		{
			final boolean is_folder_pack = file.isDirectory();
			if ( is_folder_pack )
			{
				add_pack_and_log.accept( new FolderPack( file ) );
				continue;
			}
			
			final boolean is_jar_pack = jarRegex.matcher( file.getName() ).matches();
			if ( is_jar_pack )
			{
				add_pack_and_log.accept( new JarPack( file ) );
				continue;
			}
			
			final String file_path = pack_dir.getName() + "/" + file.getName();
			logError( "fmum.unknown_pack_file_type", file_path );
		}
		
		// TODO: Post provider registry event to get providers from other mods?
//		MinecraftForge.EVENT_BUS.post( new ContentProviderRegistryEvent( providers ) );
		
		// Prepare JSON parser and type loader.
		final GsonBuilder gson_builder = new GsonBuilder();
		gson_builder.setLenient();
		gson_builder.setPrettyPrinting();
		
		Registry< BiFunction< JsonObject, Gson, ? > > type_loaders = new Registry<>();
		
		// Call prepare load for each pack.
		final IPrepareContext prepare_context = new IPrepareContext()
		{
			@Override
			public void regisGsonAdapter( Type type, JsonDeserializer< ? > adapter ) {
				gson_builder.registerTypeAdapter( type, adapter );
			}
			
			@Override
			public void regisTypeLoader( String entry, BiFunction< JsonObject, Gson, ? > loader ) {
				type_loaders.regis( entry, loader );
			}
			
			@Override
			public void regisResourceDomain( File file )
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
		this._prepareContentPackLoad( prepare_context );
		content_packs.forEach( pack -> pack.prepareLoad( prepare_context ) );
		
		final Gson gson = gson_builder.create();
		this._reloadResources();
		
		// Load content packs!
		final ILoadContext load_context = new ILoadContext()
		{
			@Override
			public Gson gson() {
				return gson;
			}
			
			@Nullable
			@Override
			public BiFunction< JsonObject, Gson, ? > getTypeLoader( String entry ) {
				return type_loaders.get( entry );
			}
		};
		content_packs.forEach( pack -> {
			logInfo( "fmum.load_content_pack", pack.sourceName() );
			pack.loadContent( load_context );
			this.loaded_packs.regis( pack );
		} );
	}
	
	protected void _prepareContentPackLoad( IPrepareContext ctx )
	{
		// Register common gson adapters.
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
		
		
		// Register capabilities.
//		ctx.regisCapability( IItem.class ); // See ItemType#CONTEXTED.
		ctx.regisCapability( PlayerPatch.class );
	}
	
	protected void _reloadResources() { }
	
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
	
	public static void sendPacketTo( IPacket packet, EntityPlayerMP player ) {
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
