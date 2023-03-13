package com.mcwb.client;

import java.util.Collection;

import com.mcwb.client.input.InputHandler;
import com.mcwb.client.player.PlayerPatchClient;
import com.mcwb.common.IAutowireLogger;
import com.mcwb.common.MCWB;
import com.mcwb.common.item.IItemType;

import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiVideoSettings;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
@EventBusSubscriber( modid = MCWBClient.ID, value = Side.CLIENT )
public final class EventHandlerClient
{
	private static final IAutowireLogger LOGGER = MCWBClient.MOD;
	
	public static boolean oriViewBobbing = MCWBClient.SETTINGS.viewBobbing;
	public static float oriMouseSensi = MCWBClient.SETTINGS.mouseSensitivity;
	
	/**
	 * Game gui in last tick
	 */
	private static GuiScreen prevGui = null;
	
	static
	{
		MinecraftForge.EVENT_BUS.register( new Object() {
			@SubscribeEvent
			public void onWorldLoad( WorldEvent.Load evt )
			{
				// Avoid model load on player local server
				if( !evt.getWorld().isRemote ) return; // Just return, do not unregister!
				
				// Call load for all subscribers
				final MCWBClient mod = MCWBClient.MOD;
				mod.meshLoadSubscribers.forEach( sub -> {
					// Throwing any exception on world load could jam the load progress and print \
					// a lot of error messages that will barely help with debug. Hence we \
					// generally want to avoid any exception being thrown out here especially when \
					// you think of that #onModelLoad method could be overridden by the pack \
					// makers who do not know this.
					try { sub.onMeshLoad(); }
					catch( Exception e ) {
						LOGGER.except( e, "mcwb.exception_call_model_load", sub );
					}
				} );
				
				// Clear resources after model load
				mod.meshLoadSubscribers.clear();
				mod.rendererPool.clear(); // TODO: check if this is needed
				mod.meshPool.clear();
				
				// Only load model once. Unregister after complete
				MinecraftForge.EVENT_BUS.unregister( this );
			}
		} );
	}
	
	private EventHandlerClient() { }
	
//	@SubscribeEvent
//	public static void onWorldUnload( WorldEvent.Unload evt )
//	{
//		final GameSettings settings = MCWBClient.SETTINGS;
//		
//		// TODO: validate if this works
//		settings.viewBobbing = oriViewBobbing;
//	}
	
	@SubscribeEvent
	public static void onGuiOpen( GuiOpenEvent evt )
	{
		final GuiScreen gui = evt.getGui();
		final GameSettings settings = MCWBClient.SETTINGS;
		
		// Show key binds if control GUI is activated
		if( gui instanceof GuiControls )
		{
			InputHandler.restoreMcKeyBinds();
			settings.mouseSensitivity = oriMouseSensi;
		}
		else if( prevGui instanceof GuiControls )
		{
			InputHandler.clearKeyMcBinds( MCWBClient.MOD.keyBindsFile );
			oriMouseSensi = settings.mouseSensitivity;
		}
		
		// Restore video settings if corresponding GUI is activated
		else if( gui instanceof GuiVideoSettings )
		{
			settings.viewBobbing = oriViewBobbing;
			// TODO: gamma lock
		}
		else if( prevGui instanceof GuiVideoSettings )
		{
			oriViewBobbing = settings.viewBobbing;
		}
		
		prevGui = gui; // Do not forget to update #prevGui!
	}
	
	@SubscribeEvent
	public static void onModelRegister( ModelRegistryEvent evt )
	{
		LOGGER.info( "mcwb.on_model_regis" );
		
		final Collection< IItemType > items = IItemType.REGISTRY.values();
		items.forEach( it -> it.onModelRegister( evt ) );
		
		LOGGER.info( "mcwb.model_regis_complete", items.size() );
	}
	
	@SubscribeEvent
	public static void onRenderGameOverlay$Pre( RenderGameOverlayEvent.Pre evt )
	{
		switch( evt.getType() )
		{
		case CROSSHAIRS:
			evt.setCanceled( PlayerPatchClient.instance.hideCrosshair() );
			break;
		default:;
		}
	}
	
	@SubscribeEvent
	public static void onRenderHand( RenderHandEvent evt ) {
		evt.setCanceled( PlayerPatchClient.instance.onRenderHandSP() );
	}
	
	/**
	 * TODO: proper intro
	 */
	@SubscribeEvent
	public static void onRenderSpecificHand( RenderSpecificHandEvent evt ) {
		evt.setCanceled( PlayerPatchClient.instance.onRenderSpecificHandSP( evt.getHand() ) );
	}
	
	/**
	 * Apply camera roll
	 */
	@SubscribeEvent
	public static void onCameraSetup( CameraSetup evt ) {
		PlayerPatchClient.instance.onCameraSetup( evt );
	}
	
	/**
	 * TODO: Apply FOV modification here for scope glass texture rendering
	 */
//	@SubscribeEvent
//	public static void onFOVModify( FOVModifier evt )
//	{
//		
//	}
	
	/**
	 * Disable dynamic FOV
	 */
	@SubscribeEvent
	public static void onFOVUpdate( FOVUpdateEvent evt ) { evt.setNewfov( 1F ); }
	
	@SubscribeEvent
	public static void onMouseInput( MouseEvent evt )
	{
		final int dWheel = evt.getDwheel();
		if( dWheel != 0 )
			evt.setCanceled( PlayerPatchClient.instance.onMouseWheelInput( dWheel ) );
	}
	
	@SubscribeEvent
	public static void onConfigChanged( OnConfigChangedEvent evt )
	{
		// Save config if has changed
		if( MCWB.ID.equals( evt.getModID() ) )
			ConfigManager.sync( MCWB.ID, Config.Type.INSTANCE );
	}
}
