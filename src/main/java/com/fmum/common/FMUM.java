package com.fmum.common;

import com.fmum.client.FMUMClient;
import com.fmum.client.ModConfigClient;
import com.fmum.common.item.IItem;
import com.fmum.common.item.IItemType;
import com.fmum.common.network.IPacket;
import com.fmum.common.network.PacketHandler;
import com.fmum.common.pack.IContentBuildContext;
import com.fmum.common.pack.IContentLoader;
import com.fmum.common.pack.IContentPack;
import com.fmum.common.pack.ILoadablePack;
import com.fmum.common.pack.ILoadablePack.IPrepareContext;
import com.fmum.common.pack.ILoadedPack;
import com.fmum.common.pack.IPreparedPack;
import com.fmum.common.pack.IPreparedPack.ILoadContext;
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
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
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
import java.util.function.Function;
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
	
	public final Registry< IContentPack > content_packs = new Registry<>( IContentPack::name );
	
	protected final PacketHandler packet_handler = new PacketHandler( MODID );
	
	protected File config_dir;
	
	private final LinkedList< ILoadedPack > unfinalized_packs = new LinkedList<>();
	
	/**
	 * @see #logInfo(String, Object...)
	 * @see #logWarning(String, Object...)
	 * @see #logError(String, Object...)
	 * @see #logException(Throwable, String, Object...)
	 */
	private Logger logger;
	
	private ICreativeTab default_creative_tab = new ICreativeTab()
	{
		private CreativeTabs vanilla_tab;
		
		@Override
		public String name() {
			return MODID;
		}
		
		@Override
		public void regisItem( IItemType item )
		{
			this.vanilla_tab = Optional.ofNullable( this.vanilla_tab )
				.orElseGet( this::vanillaCreativeTab );
			item.vanillaItem().setCreativeTab( this.vanilla_tab );
		}
		
		@Override
		public CreativeTabs vanillaCreativeTab()
		{
			return new CreativeTabs( MODID )
			{
				@Override
				@SideOnly( Side.CLIENT )
				public ItemStack createIcon()
				{
					final Item icon_item = Item.getByNameOrId(
						ModConfigClient.default_creative_tab_icon_item );
					return new ItemStack( Optional.ofNullable( icon_item ).orElseGet( () -> {
					
					} ) );
				}
			};
		}
	};
	
	protected FMUM() { }
	
	protected ICreativeTab _createDefaultTab()
	{
	
	}
	
	protected ICreativeTab _createHideTab()
	{
	
	}
	
	private IContentBuildContext __fallbackBuildCtx( String fallback_name )
	{
		return new IContentBuildContext()
		{
			@Override
			public String fallbackName() {
				return fallback_name;
			}
			
			@Override
			public IContentPack contentPack() {
				return null;
			}
			
			@Override
			public Gson gson() {
				return null;
			}
			
			@Override
			public void regisPostLoadCallback( Runnable callback ) {
				callback.run();
			}
		};
	}
	
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
		
		this.__finalizePacksAndPacketHandler();
		
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
	
	/**
	 * Use this to do localization if your code runs on both side to avoid crash.
	 */
	@SuppressWarnings( "deprecation" )
	public String format( String translate_key, Object... parameters )
	{
		return net.minecraft.util.text.translation
.			I18n.translateToLocalFormatted( translate_key, parameters );
	}
	
	public boolean isClient() {
		return false;
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
	
	public final void logException( Throwable e, String translate_key, Object... parameters ) {
		this.logger.error( this.format( translate_key, parameters ), e );
	}
	
	protected void _loadContentPacks()
	{
		final LinkedList< ILoadablePack > loadable_packs = new LinkedList<>();
		this.__forEachPackInModFolder( ( pack, source_name ) -> {
			loadable_packs.add( pack );
			this.logInfo( "fmum.detect_content_pack", source_name );
		} );
		
		// Prepare JSON parser and type loader.
		final GsonBuilder gson_builder = new GsonBuilder();
		gson_builder.setLenient();
		gson_builder.setPrettyPrinting();
		
		final Registry< IContentLoader > content_loaders = new Registry<>();
		
		// Call prepare load for each pack.
		final LinkedList< IPreparedPack > prepared_packs = new LinkedList<>();
		final IPrepareContext prepare_context = new IPrepareContext()
		{
			@Override
			public void regisGsonAdapter( Type type, JsonDeserializer< ? > adapter ) {
				gson_builder.registerTypeAdapter( type, adapter );
			}
			
			@Override
			public void regisContentLoader( String entry, IContentLoader loader ) {
				content_loaders.regis( entry, loader );
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
		final Function< ILoadablePack, IPreparedPack >
			callPrepare = this._callPackPrepareLoad( prepare_context );
		loadable_packs.forEach( pack -> prepared_packs.add( callPrepare.apply( pack ) ) );
		
		final Gson gson = gson_builder.create();
		
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
		prepared_packs.forEach( pack -> {
			final ILoadedPack loaded_pack = pack.loadPack( load_context );
			this.unfinalized_packs.add( loaded_pack );
		} );
	}
	
	protected Function< ILoadablePack, IPreparedPack > _callPackPrepareLoad( IPrepareContext ctx ) {
		return pack -> pack.prepareLoadServerSide( ctx );
	}
	
	protected void _regisGsonAdapter( IPrepareContext ctx )
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
	
	private void __regisCapability( IPrepareContext ctx )
	{
		ctx.regisCapability( IItem.class );  // See ItemType#CAPABILITY.
		ctx.regisCapability( PlayerPatch.class );
	}
	
	private void __forEachPackInModFolder( BiConsumer< ILoadablePack, String > visitor )
	{
		final Loader loader_ctx = Loader.instance();
		final ArtifactVersion version = loader_ctx.activeModContainer().getProcessedVersion();
		loader_ctx.getActiveModList().forEach( mod_container -> {
			for ( ArtifactVersion requirement : mod_container.getRequirements() )
			{
				final boolean is_mod_based_pack = MODID.equals( requirement.getLabel() );
				if ( !is_mod_based_pack ) {
					continue;
				}
				
				// TODO: Check why this is predicated as always false by IDEA.
				final boolean is_compatible_core_version = requirement.containsVersion( version );
				if ( !is_mod_based_pack )
				{
					this.logError(
						"fmum.incompatible_core_version",
						version.getVersionString(), requirement.getRangeString()
					);
					break;
				}
				
				final Object pack_mod = mod_container.getMod();
				final boolean is_correct_implementation = pack_mod instanceof ILoadablePack;
				if ( !is_correct_implementation )
				{
					this.logError(
						"fmum.invalid_mod_based_pack",
						mod_container.getName(), mod_container.getModId()
					);
					break;
				}
				
				final String source_name = mod_container.getSource().getName();
				visitor.accept( ( ILoadablePack ) pack_mod, source_name );
				break;
			}
		} );
	}
	
	private void __finalizePacksAndPacketHandler()
	{
		this.unfinalized_packs.forEach( pack -> {
			final IContentPack finalized_pack = pack.finalizePack();
			this.content_packs.regis( finalized_pack );
		} );
		this.unfinalized_packs.clear();
		
		this.packet_handler.regisPackets();
	}
	
	private void __printAllLoadedPacks()
	{
		this.logInfo( "fmum.total_loaded_packs", String.valueOf( this.content_packs.size() ) );
		this.content_packs.values().forEach( pack ->
			this.logInfo( "fmum.info_loaded_pack", pack.name(), pack.author() ) );
	}
}
