package com.fmum;

import com.fmum.animation.Sounds;
import com.fmum.network.IPacket;
import com.fmum.network.PacketSyncConfig;
import gsf.devtool.Dev;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GLContext;

import java.util.List;

@Mod(
	modid = FMUM.MODID,
	version = FMUM.VERSION,
//	clientSideOnly = true,
	// TODO: this needs update.
	updateJSON = "https://raw.githubusercontent.com/Giant-Salted-Fish/Key-Binding-Patch/1.16.X/update.json",
	acceptedMinecraftVersions = "[1.12,1.13)",
//	guiFactory = "com.fmum.ConfigGuiFactory",
	dependencies = "required-client:key_binding_patch@[1.12.2-1.3.2.0,1.12.2-1.4.0.0);"
)
public final class FMUM
{
	public static final String MODID = "fmum";
	public static final String VERSION = "1.12.2-0.2.0.0-alpha";
	
	/**
	 * Helper for side-dependent code.
	 */
	public static final WrappedSide SIDE;
	
	static
	{
		final Side side = FMLCommonHandler.instance().getSide();
		if ( side.isServer() )
		{
			SIDE = new WrappedSide() {
				@Override
				public Side getSide() {
					return Side.SERVER;
				}
				
				@Override
				public void runIfClient( Runnable task ) {
					// Pass.
				}
			};
		}
		else
		{
			SIDE = new WrappedSide() {
				@Override
				public Side getSide() {
					return Side.CLIENT;
				}
				
				@Override
				public void runIfClient( Runnable task ) {
					task.run();
				}
			};
		}
	}
	
	/**
	 * A wrapped logger for convenient use.
	 */
	public static final WrappedLogger LOGGER = new WrappedLogger() {
		@Override
		public void info( String message, Object... params ) {
			raw_logger.info( message, params );
		}
		
		@Override
		public void warn( String message, Object... params ) {
			raw_logger.warn( message, params );
		}
		
		@Override
		public void error( String message, Object... params ) {
			raw_logger.error( message, params );
		}
		
		@Override
		public void exception( Throwable e, String message, Object... params )
		{
			raw_logger.error( message, params );
			raw_logger.catching( e );
		}
		
		@Override
		public Logger unwrap() {
			return raw_logger;
		}
	};
	
	public static final WrappedNet NET = new WrappedNet() {
		@Override
		public void sendPacketS2C( IPacket packet, EntityPlayerMP player ) {
			net_wrapper.sendTo( packet, player );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public void sendPacketC2S( IPacket packet ) {
			net_wrapper.sendToServer( packet );
		}
	};
	
	
	private static Logger raw_logger;
	private static SimpleNetworkWrapper net_wrapper;
	
	private PackLoader pack_loader;
	
	
	private FMUM()
	{
		MinecraftForge.EVENT_BUS.register( this );
		MinecraftForge.EVENT_BUS.register( new Object() {
			@SubscribeEvent
			void _onSoundRegis( RegistryEvent.Register< SoundEvent > evt )
			{
				final IForgeRegistry< SoundEvent > registry = evt.getRegistry();
				registry.register( Sounds.LOAD_AMMO );
				registry.register( Sounds.UNLOAD_AMMO );
				FMUM.this.pack_loader._regisSounds( registry );
				MinecraftForge.EVENT_BUS.unregister( this );
			}
		} );
		MinecraftForge.EVENT_BUS.register( new Object() {
			@SubscribeEvent
			@SideOnly( Side.CLIENT )
			void _onGuiChange( GuiOpenEvent evt )
			{
				// Because SplashProgress#finish() will switch the GLContext,
				// we need to delay the mesh load till the first event after
				// the context switch, which is this event.
				if ( evt.getGui() instanceof GuiMainMenu )
				{
					FMUM.this.pack_loader._loadMeshForPacks();
					FMUM.this.pack_loader = null;
					MinecraftForge.EVENT_BUS.unregister( this );  // Run once.
				}
			}
		} );
		MinecraftForge.EVENT_BUS.register( new Object() {
			@SubscribeEvent
			@SideOnly( Side.SERVER )
			void _onWorldLoad( WorldEvent.Load evt )
			{
				// Update sync config for dedicated server.
				// For client side, see PacketSyncConfig.
				SyncConfig.max_module_depth = ModConfig.max_module_depth;
				SyncConfig.max_slot_capacity = ModConfig.max_slot_capacity;
				MinecraftForge.EVENT_BUS.unregister( this );
			}
		} );
	}
	
	@EventHandler
	private void __onPreInit( FMLPreInitializationEvent evt )
	{
		raw_logger = evt.getModLog();
		LOGGER.info( "Pre-initializing FMUM..." );
		
		// Check render device capabilities.
		SIDE.runIfClient( () -> {
			if ( !GLContext.getCapabilities().OpenGL30 ) {
				throw new RuntimeException( "Your OpenGL version is too low! Minimum requirement is OpenGL3.0. Please upgrade your GPU or driver." );
			}
			
			final Framebuffer framebuffer = Minecraft.getMinecraft().getFramebuffer();
			if ( !framebuffer.isStencilEnabled() && !framebuffer.enableStencil() ) {
				throw new RuntimeException( "Stencil test not supported. Please upgrade your GPU or driver." );
			}
		} );
		
		// Load content packs.
		final Loader loader = Loader.instance();
		final ModContainer self_container = loader.activeModContainer();
		final List< ModContainer > active_mod_list = loader.getActiveModList();
		this.pack_loader = new PackLoader( self_container, active_mod_list );
		this.pack_loader._preLoadPacks();
		this.pack_loader._loadPacks();
		FMUM.SIDE.runIfClient( Dev::init );
		
		LOGGER.info( "FMUM pre-initialization complete." );
	}
	
	@EventHandler
	private void __onInit( FMLInitializationEvent evt )
	{
		LOGGER.info( "Initializing FMUM..." );
		
		net_wrapper = new NetBuilder( MODID )._regisPackets()._build();
		this.pack_loader._postLoadPacks();
		
		LOGGER.info( "FMUM initialization complete." );
	}
	
	@EventHandler
	private void __onPostInit( FMLPostInitializationEvent evt )
	{
		LOGGER.info( "Post-initializing FMUM..." );
		
		// TODO: Whether to support packets registration for packets?
//		NET.postInit();
		
		LOGGER.info( "FMUM post-initialization complete." );
	}
	
	@SubscribeEvent
	@SideOnly( Side.CLIENT )
	void _onModConfigChanged( OnConfigChangedEvent evt )
	{
		if ( evt.getModID().equals( MODID ) ) {
			ConfigManager.sync( MODID, Type.INSTANCE );
		}
	}
	
	// This event seems to only be posted on server side.
	@SubscribeEvent
	void _onPlayerLogin( PlayerLoggedInEvent evt )
	{
		final EntityPlayerMP player = ( EntityPlayerMP ) evt.player;
		NET.sendPacketS2C( new PacketSyncConfig( null ), player );
	}
	
	@Mod.InstanceFactory
	@SuppressWarnings( "unused" )
	private static FMUM __create() {
		return new FMUM();
	}
}
