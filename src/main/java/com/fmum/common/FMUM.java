package com.fmum.common;

import com.fmum.client.FMUMClient;
import com.fmum.client.ModConfigClient;
import com.fmum.common.gun.GunPartType;
import com.fmum.common.item.IItem;
import com.fmum.common.item.IItemType;
import com.fmum.common.load.BuildableType;
import com.fmum.common.load.IContentBuildContext;
import com.fmum.common.load.IContentLoader;
import com.fmum.common.module.IModuleSlot;
import com.fmum.common.module.StepRailSlot;
import com.fmum.common.pack.IContentPackFactory.IMeshLoadContext;
import com.fmum.util.CategoryDomain;
import com.fmum.util.Category;
import com.fmum.common.network.IPacket;
import com.fmum.common.network.PacketHandler;
import com.fmum.common.pack.IContentPack;
import com.fmum.common.pack.IContentPackFactory;
import com.fmum.common.pack.IContentPackFactory.ILoadContext;
import com.fmum.common.pack.IContentPackFactory.IPostLoadContext;
import com.fmum.common.pack.IContentPackFactory.IPrepareContext;
import com.fmum.common.pack.LocalPack;
import com.fmum.common.paintjob.JsonPaintjob;
import com.fmum.common.player.PlayerPatch;
import com.fmum.common.tab.JsonCreativeTab;
import com.fmum.common.tab.ICreativeTab;
import com.fmum.util.AngleAxis4f;
import com.fmum.util.Quat4f;
import com.fmum.util.Vec3f;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * {@link FMUM} provides a platform to load highly customizable weapons.
 *
 * @author Giant_Salted_Fish
 */
@Mod(
	modid = FMUM.MODID,
	name = FMUM.MOD_NAME,
	version = FMUM.MOD_VERSION,
	acceptedMinecraftVersions = "1.12.2",
	guiFactory = "com.fmum.client.ConfigGuiFactory"
//	, clientSideOnly = true
)
public class FMUM
{
	public static final String MODID = "fmum";
	public static final String MOD_NAME = "FMUM 2.0";
	public static final String MOD_VERSION = "0.3.3";
	
	public static final FMUM MOD;
	static
	{
		final Supplier< ? > client_mod = () -> FMUMClient.MOD;  // Use generic will crash.
		final Side side = FMLCommonHandler.instance().getSide();
		MOD = side.isServer() ? new FMUM() : ( FMUM ) client_mod.get();
	}
	@Mod.InstanceFactory
	private static FMUM create() { return MOD; }
	
	public static int max_slot_capacity;
	
	public final Registry< IContentPack >
		content_packs = new Registry<>( IContentPack::name );
	
	protected final PacketHandler packet_handler = new PacketHandler( MODID );
	
	protected File config_dir;
	
	/**
	 * @see #logInfo(String, Object...)
	 * @see #logWarning(String, Object...)
	 * @see #logError(String, Object...)
	 * @see #logException(Throwable, String, Object...)
	 */
	private Logger logger;
	
	private Runnable post_load_callback;
	
	protected FMUM() { }
	
	@Mod.EventHandler
	private void onPreInit( FMLPreInitializationEvent evt )
	{
		this.logger = evt.getModLog();
		
		this.logInfo( "fmum.on_pre_init" );
		
		this.config_dir = evt.getModConfigurationDirectory();
		this._loadContentPacks();
		
		this.logInfo( "fmum.pre_init_complete" );
	}
	
	@Mod.EventHandler
	private void onInit( FMLInitializationEvent evt )
	{
		this.logInfo( "fmum.on_init" );
		
		this.post_load_callback.run();
		this.post_load_callback = null;
		this.packet_handler.regisPackets();
		
		this.logInfo( "fmum.init_complete" );
		
		this.__printAllLoadedPacks();
	}
	
	@Mod.EventHandler
	private void onPostInit( FMLPostInitializationEvent evt )
	{
		this.logInfo( "fmum.on_post_init" );
		
		// TODO: Whether to support packets registration for packs?
//		this.packet_handler.postInit();
		
		this.logInfo( "fmum.post_init_complete" );
	}
	
	/**
	 * Send packet to client.
	 */
	public final void sendPacketS2C( IPacket packet, EntityPlayerMP player ) {
		this.packet_handler.sendTo( packet, player );
	}
	
	public boolean isClient() {
		return false;
	}
	
	/**
	 * Use this to do localization if your code runs on both side to avoid crash.
	 */
	@SuppressWarnings( "deprecation" )
	public String format( String translate_key, Object... parameters )
	{
		return net.minecraft.util.text.translation
.			I18n.translateToLocalFormatted( translate_key, parameters );
	}
	
	public final void logInfo( String translate_key, Object... parameters ) {
		this.logger.info( this.format( translate_key, parameters ) );
	}
	
	public final void logWarning( String translate_key, Object... parameters ) {
		this.logger.warn( this.format( translate_key, parameters ) );
	}
	
	public final void logError( String translate_key, Object... parameters ) {
		this.logger.error( this.format( translate_key, parameters ) );
	}
	
	public final void logException(
		Throwable e, String translate_key, Object... parameters
	) { this.logger.error( this.format( translate_key, parameters ), e ); }
	
	protected void _loadContentPacks()
	{
		final LinkedList< IContentPackFactory >
			pack_factories = new LinkedList<>();
		this.__forEachPackFactoryInModFolder( ( factory, source ) -> {
			pack_factories.add( factory );
			this.logInfo( "fmum.detect_content_pack", source );
		} );
		
		// Prepare JSON parser and content loader.
		final GsonBuilder gson_builder = new GsonBuilder();
		gson_builder.setLenient();
		gson_builder.setPrettyPrinting();
		
		final Registry< IContentLoader > content_loaders = new Registry<>();
		
		// Prepare pack load.
		final LinkedList< Consumer< ILoadContext > >
			load_callbacks = new LinkedList<>();
		final LinkedList< Consumer< IPostLoadContext > >
			post_load_callbacks = new LinkedList<>();
		final IPrepareContext prepare_context = new IPrepareContext()
		{
			@Override
			public void regisLoadCallback( Consumer< ILoadContext > callback ) {
				load_callbacks.add( callback );
			}
			
			@Override
			public void regisPostLoadCallback(
				Consumer< IPostLoadContext > callback
			) { post_load_callbacks.add( callback ); }
			
			@Override
			public void regisGsonDeserializer(
				Type type, JsonDeserializer< ? > adapter
			) { gson_builder.registerTypeAdapter( type, adapter ); }
			
			@Override
			public void regisGsonSerializer(
				Type type, JsonSerializer< ? > adapter
			) { gson_builder.registerTypeAdapter( type, adapter ); }
			
			@Override
			public void regisContentLoader(
				String entry, IContentLoader loader
			) { content_loaders.regis( entry, loader ); }
			
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
		pack_factories.forEach( factory -> {
			final IContentPack content_pack =
				this._callCreatePackOnSide( factory, prepare_context );
			this.content_packs.regis( content_pack );
		} );
		
		final Gson gson = gson_builder.create();
		
		// Fire load callbacks.
		final ILoadContext load_context = new ILoadContext()
		{
			@Override
			public void regisPostLoadCallback(
				Consumer< IPostLoadContext > callback
			) { post_load_callbacks.add( callback ); }
			
			@Override
			@SideOnly( Side.CLIENT )
			public void regisMeshLoadCallback(
				Consumer< IMeshLoadContext > callback
			) { FMUM.this._regisMeshLoadCallback( callback ); }
			
			@Override
			public Gson gson() {
				return gson;
			}
			
			@Override
			public Optional< IContentLoader > getContentLoader( String entry ) {
				return Optional.ofNullable( content_loaders.get( entry ) );
			}
		};
		load_callbacks.forEach( callback -> callback.accept( load_context ) );
		
		this._loadKeyBindSetting();
		
		// Setup post load callback.
		this.post_load_callback = () -> {
			final ICreativeTab default_tab = this.__createDefaultTab();
			final ICreativeTab hidden_tab = this.__createHiddenTab();
			final IPostLoadContext post_load_context = new IPostLoadContext()
			{
				@Override
				@SideOnly( Side.CLIENT )
				public ItemStack defaultTabIconItem() {
					return FMUM.this.__createDefaultTabIconItem();
				}
				
				@Override
				public ICreativeTab defaultCreativeTab() {
					return default_tab;
				}
				
				@Override
				public ICreativeTab hideCreativeTab() {
					return hidden_tab;
				}
			};
			post_load_callbacks.forEach(
				callback -> callback.accept( post_load_context ) );
		};
	}
	
	protected IContentPack _callCreatePackOnSide(
		IContentPackFactory factory, IPrepareContext ctx
	) { return factory.createServerSide( ctx ); }
	
	protected void _loadKeyBindSetting() { }
	
	@SideOnly( Side.CLIENT )
	protected void _regisMeshLoadCallback(
		Consumer< IMeshLoadContext > callback
	) { }
	
	protected void _regisGsonAdapter( IPrepareContext ctx )
	{
		ctx.regisGsonDeserializer(
			IModuleSlot.class,
			( json, type_of_T, context ) -> context.deserialize( json, StepRailSlot.class )
		);
		
		ctx.regisGsonDeserializer(
			Category.class,
			( json, type_of_T, context ) ->
				new Category( json.getAsString() )
		);
		
		ctx.regisGsonDeserializer(
			CategoryDomain.class,
			( json, type_of_T, context ) -> new CategoryDomain( json )
		);
		
		ctx.regisGsonDeserializer(
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
		
		ctx.regisGsonDeserializer(
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
		
		ctx.regisGsonDeserializer(
			Quat4f.class,
			( json, type_of_T, context ) -> {
				final AngleAxis4f rot = context.deserialize(
					json, AngleAxis4f.class );
				return new Quat4f( rot );
			}
		);
	}
	
	protected void _regisContentLoader( IPrepareContext ctx )
	{
		this._quickRegisContentLoader(
			ctx, "creative_tab",
			JsonCreativeTab.class,
			JsonCreativeTab::buildServerSide
		);
		this._quickRegisContentLoader(
			ctx, "paintjob",
			JsonPaintjob.class,
			JsonPaintjob::buildServerSide
		);
		this._quickRegisContentLoader(
			ctx, "gun_part",
			GunPartType.class,
			GunPartType::buildServerSide
		);
	}
	
	protected final < T > void _quickRegisContentLoader(
		IPrepareContext ctx,
		String entry,
		Class< T > clazz,
		BiConsumer< T, IContentBuildContext > processor
	) {
		ctx.regisContentLoader( entry, ( obj, gson, ctx_ ) -> {
			final T buildable = gson.fromJson( obj, clazz );
			processor.accept( buildable, ctx_ );
			return buildable;
		} );
	}
	
	private void __regisCapability( IPrepareContext ctx )
	{
		ctx.regisCapability( IItem.class );  // See ItemType#CAPABILITY.
		ctx.regisCapability( PlayerPatch.class );
	}
	
	private void __forEachPackFactoryInModFolder(
		BiConsumer< IContentPackFactory, String > visitor
	) {
		final Loader loader_ctx = Loader.instance();
		final ModContainer self_container = loader_ctx.activeModContainer();
		final File core_file = self_container.getSource();
		final IContentPackFactory core_factory = new LocalPack( self_container )
		{
			@Override
			protected void _loadPackContent( ILoadContext ctx ) { }
		};
		visitor.accept( core_factory, core_file.getName() );
		
		final ArtifactVersion version = self_container.getProcessedVersion();
		loader_ctx.getActiveModList().forEach( mod_container -> {
			for ( ArtifactVersion requirement : mod_container.getRequirements() )
			{
				final boolean is_requirement_match =
					MODID.equals( requirement.getLabel() );
				if ( !is_requirement_match ) {
					continue;
				}
				
				// TODO: And make version as a specially constructed range to test compatible version.
				final boolean is_compatible_core_version =
					requirement.containsVersion( version );
				if ( !is_compatible_core_version )
				{
					this.logError(
						"fmum.incompatible_core_version",
						version.getVersionString(),
						requirement.getRangeString()
					);
					break;
				}
				
				final Object pack_mod = mod_container.getMod();
				final boolean is_correct_implementation =
					pack_mod instanceof IContentPackFactory;
				if ( !is_correct_implementation )
				{
					this.logError(
						"fmum.invalid_mod_based_pack",
						mod_container.getName(),
						mod_container.getModId()
					);
					break;
				}
				
				final String source_name = mod_container.getSource().getName();
				visitor.accept( ( IContentPackFactory ) pack_mod, source_name );
				break;
			}
		} );
	}
	
	private ICreativeTab __createDefaultTab()
	{
		final ICreativeTab tab = new ICreativeTab()
		{
			private Supplier< CreativeTabs > vanilla_tab = () -> {
				final CreativeTabs tabs = new CreativeTabs( MODID )
				{
					@Override
					@SideOnly( Side.CLIENT )
					public ItemStack createIcon() {
						return FMUM.this.__createDefaultTabIconItem();
					}
				};
				this.vanilla_tab = () -> tabs;  // Only create once.
				return tabs;
			};
			
			@Override
			public String name() {
				return MODID;
			}
			
			@Override
			public CreativeTabs vanillaCreativeTab() {
				return this.vanilla_tab.get();
			}
		};
		ICreativeTab.REGISTRY.regis( tab );
		return tab;
	}
	
	private ICreativeTab __createHiddenTab()
	{
		final ICreativeTab tab = new ICreativeTab()
		{
			@Override
			public String name() {
				return "hide";
			}
			
			@Override
			public CreativeTabs vanillaCreativeTab() {
				return null;
			}
		};
		ICreativeTab.REGISTRY.regis( tab );
		return tab;
	}
	
	@SideOnly( Side.CLIENT )
	private ItemStack __createDefaultTabIconItem()
	{
		final String icon_item = ModConfigClient.default_creative_tab_icon_item;
		final short meta = ModConfigClient.default_creative_tab_icon_item_meta;
		return IItemType.findItem( icon_item )
			.map( item -> new ItemStack( item, 1, meta ) )
			.orElseGet( () -> new ItemStack( Items.FISH ) );
	}
	
	private void __printAllLoadedPacks()
	{
		final String num_packs = String.valueOf( this.content_packs.size() );
		this.logInfo( "fmum.total_loaded_packs", num_packs );
		this.content_packs.values().forEach( pack ->
			this.logInfo( "fmum.info_loaded_pack", pack.name(), pack.author() ) );
	}
}
