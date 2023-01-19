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
import com.mcwb.common.gun.GunMeta;
import com.mcwb.common.gun.GunPartMeta;
import com.mcwb.common.gun.IContextedGun;
import com.mcwb.common.gun.IContextedGunPart;
import com.mcwb.common.item.ModifiableItemMeta;
import com.mcwb.common.load.BuildableLoader;
import com.mcwb.common.load.IRequireMeshLoad;
import com.mcwb.common.load.IRequirePostLoad;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.meta.Registry;
import com.mcwb.common.modify.IContextedModifiable;
import com.mcwb.common.modify.IModuleSlot;
import com.mcwb.common.modify.IModuleSnapshot;
import com.mcwb.common.network.PacketHandler;
import com.mcwb.common.pack.AbstractLocalPack;
import com.mcwb.common.pack.FolderPack;
import com.mcwb.common.pack.IContentProvider;
import com.mcwb.common.pack.JarPack;
import com.mcwb.common.paintjob.IPaintjob;
import com.mcwb.common.player.PlayerPatch;
import com.mcwb.common.tab.CreativeTab;
import com.mcwb.util.Vec3f;

import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.nbt.NBTBase;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
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
import net.minecraftforge.fml.relauncher.Side;

/**
 * A weapon framework that provides highly customizable weapons in {@link Minecraft}
 * 
 * @author Giant_Salted_Fish
 */
@Mod(
	modid = MCWB.MODID,
	name = MCWB.NAME,
	version = MCWB.VERSION,
	acceptedMinecraftVersions = "[1.12, 1.13)",
	guiFactory = "com.mcwb.client.ConfigGuiFactory"
//	, clientSideOnly = true
)
public class MCWB extends URLClassLoader implements IContentProvider, IAutowireLogger
{
	/**
	 * Mod id
	 */
	public static final String MODID = "mcwb";
	
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
	 * Network packet handler
	 */
	public static final PacketHandler NET = new PacketHandler( MODID );
	
	/**
	 * A default {@link Gson} parser to load types in content pack
	 */
	public static final Gson GSON = MOD.newGsonBuilder().create();
	
	/**
	 * Type and model loaders
	 */
	public static final Registry< BuildableLoader< ? extends IMeta > >
		TYPE_LOADERS = new Registry<>();
	
	/**
	 * Default creative item tab
	 */
	public static final CreativeTab DEF_TAB = new CreativeTab().build( MODID, MOD );
	
	/**
	 * Items added into this tab will not be shown in creative item tab
	 */
	public static final CreativeTab HIDE_TAB = new CreativeTab() {
		@Override
		protected CreativeTabs createTab() { return null; }
	}.build( "hide", MOD );
	
	/**
	 * All loaded content packs
	 */
	protected static final Registry< IContentProvider > CONTENT_PROVIDERS = new Registry<>();
	
	/**
	 * Registered subscribers wait for post load callback
	 */
	protected static final LinkedList< IRequirePostLoad > POST_LOAD_SUBSCRIBERS = new LinkedList<>();
	
	/**
	 * Buffered sounds
	 */
	protected static final HashMap< String, SoundEvent > SOUND_POOL = new HashMap<>();
	
	/**
	 * ".minecraft/" folder
	 */
	protected static final File GAME_DIR = Loader.instance().getConfigDir().getParentFile();
	
	/**
	 * Do not directly refer to this field. Use {@link IAutowireLogger} instead.
	 */
	static final Logger LOGGER = LogManager.getLogger( MODID );
	
	private final HashMap< String, byte[] > classes = new HashMap<>();
	
	public MCWB() { super( new URL[ 0 ], MinecraftServer.class.getClassLoader() ); }
	
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
		this.regisCapability( IContextedModifiable.class );
		this.regisCapability( IContextedGunPart.class );
		this.regisCapability( IContextedGun.class );
		
		// Register meta loaders
		TYPE_LOADERS.regis( CreativeTab.LOADER );
		TYPE_LOADERS.regis( GunPartMeta.LOADER );
		TYPE_LOADERS.regis( GunMeta.LOADER );
		this.setupSideDependentLoaders();
	}
	
	@Override
	public void load()
	{
		// Check content pack folder
		// TODO: if load packs from mods dir then allow the player to disable content pack folder
		final File packDir = new File( GAME_DIR, MODID );
		if( !packDir.exists() )
		{
			packDir.mkdirs();
			this.info( "mcwb.pack_dir_created", MODID );
		}
		
		// Compile a regex to match the supported content pack file types
		// TODO: check if zip is supported
		final Pattern packPattern = Pattern.compile( "(.+)\\.(zip|jar)$" );
		
		// Find all content packs and load them
		final HashSet< IContentProvider > providers = new HashSet<>();
		for( final File file : packDir.listFiles() )
		{
			AbstractLocalPack pack;
			if( file.isDirectory() )
				pack = new FolderPack( file );
			else if( packPattern.matcher( file.getName() ).matches() )
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
		
		// Let content packs register their resource domains and reload Minecraft resources
		providers.forEach( IContentProvider::preLoad );
		this.reloadResources();
		
		// Load content packs!
		providers.forEach( p -> {
			this.info( "mcwb.load_content_pack", p.sourceName() );
			p.load();
			CONTENT_PROVIDERS.regis( p );
		} );
		
		// Fire load callback
		POST_LOAD_SUBSCRIBERS.forEach( IRequirePostLoad::onPostLoad );
		POST_LOAD_SUBSCRIBERS.clear();
	}
	
	@Override
	public void regisPostLoad( IRequirePostLoad subscriber ) {
		POST_LOAD_SUBSCRIBERS.add( subscriber );
	}
	
	@Override
	public void regisMeshLoad( IRequireMeshLoad subscriber ) { }
	
	public void regisResourceDomain( File resource )
	{
		try { this.addURL( resource.toURI().toURL() ); }
		catch( MalformedURLException e ) {
			this.except( e, "mcwb.error_adding_classpath", resource.getName() );
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
	
	/**
	 * @return {@code null} on {@link Side#SERVER}
	 */
	@Nullable
	public ResourceLocation loadTexture( String path ) { return null; }
	
	public final SoundEvent loadSound( String path )
	{
		return SOUND_POOL.computeIfAbsent(
			path, key -> new SoundEvent( new ResourceLocation( MODID, key ) )
		);
	}
	
	public boolean isClient() { return false; }
	
//	public < T > T sideOnly( Supplier< ? extends T > common, Supplier< ? extends T > client ) {
//		return common.get();
//	}
	
	/**
	 * This localize the message based on the physical side. Use this for localization if the
	 * message may be formated in both client side and server side.
	 * 
	 * @see net.minecraft.client.resources.I18n#format(String, Object...)
	 */
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
	public String author() { return "mcwb.core_author"; }
	
	@Override
	public String sourceName() { return NAME; }
	
	protected void setupSideDependentLoaders()
	{
		// Key binds is client only. But to avoid the errors when it is absent, set a loader that \
		// simply does nothing on load. 
		TYPE_LOADERS.regis(
			new BuildableLoader<>( "key_bind", json -> ( name, provider ) -> null )
		);
	}
	
	/**
	 * {@link Side#CLIENT} only
	 * 
	 * TODO: check if needed server side to load languages
	 */
	protected void reloadResources() { }
	
	protected GsonBuilder newGsonBuilder()
	{
		final GsonBuilder builder = new GsonBuilder();
		builder.setLenient();
		builder.setPrettyPrinting();
		
		final Gson vecParser = new GsonBuilder().setLenient().create();
		builder.registerTypeAdapter(
			Vec3f.class,
			( JsonDeserializer< Vec3f > ) ( json, typeOfT, context ) -> {
				if( json.isJsonObject() )
					return vecParser.fromJson( json, Vec3f.class );
				
				final Vec3f vec = new Vec3f();
				if( json.isJsonArray() )
				{
					final JsonArray arr = json.getAsJsonArray();
					switch( arr.size() )
					{
					case 3: vec.z = arr.get( 2 ).getAsFloat();
					case 2: vec.y = arr.get( 1 ).getAsFloat();
					case 1: vec.x = arr.get( 0 ).getAsFloat();
					}
				} // TODO: maybe add support for string
				return vec;
			}
		);
		builder.registerTypeAdapter( IModuleSlot.class, ModifiableItemMeta.SLOT_ADAPTER );
		builder.registerTypeAdapter(
			IModuleSnapshot.class, ModifiableItemMeta.MODULE_SNAPSHOT_ADAPTER
		);
		builder.registerTypeAdapter( IPaintjob.class, ModifiableItemMeta.PAINTJOB_ADAPTER );
		return builder;
	}
	
	@Override
	protected Class< ? > findClass( String name ) throws ClassNotFoundException
	{
		final byte[] bytes = this.classes.get( name );
		return(
			bytes != null
			? this.defineClass( name, bytes, 0, bytes.length )
			: super.findClass( name )
		);
	}
}
