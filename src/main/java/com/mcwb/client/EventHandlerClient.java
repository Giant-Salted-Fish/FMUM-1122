package com.mcwb.client;

import java.util.Collection;

import org.lwjgl.opengl.GL11;

import com.mcwb.client.input.IKeyBind;
import com.mcwb.client.input.InputHandler;
import com.mcwb.client.player.PlayerPatchClient;
import com.mcwb.common.IAutowireLogger;
import com.mcwb.common.MCWB;
import com.mcwb.common.item.IItemMeta;
import com.mcwb.util.Mesh;
import com.mcwb.util.ObjMeshBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiVideoSettings;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
@EventBusSubscriber( modid = MCWBClient.MODID, value = Side.CLIENT )
public final class EventHandlerClient
{
	private static final IAutowireLogger LOGGER = MCWBClient.MOD;
	
	public static boolean oriViewBobbing = MCWBClient.SETTINGS.viewBobbing;
	public static float oriMouseSensi = MCWBClient.SETTINGS.mouseSensitivity;
	
	/**
	 * Game gui in last tick
	 */
	private static GuiScreen prevGui = null;
	
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
		
		// Do not forget to update #prevGui
		prevGui = gui;
	}
	
	@SubscribeEvent
	public static void onModelRegister( ModelRegistryEvent evt )
	{
		LOGGER.info( "mcwb.on_model_regis" );
		
		final Collection< IItemMeta > items = IItemMeta.REGISTRY.values();
		items.forEach( it -> it.onModelRegister( evt ) );
		
		LOGGER.info( "mcwb.model_regis_complete", items.size() );
	}
	
	private static boolean modelLoaded = false;
	/**
	 * 3D models will be build client side on the first time of the world load
	 */
	@SubscribeEvent
	public static void onWorldLoad( WorldEvent.Load evt )
	{
		// Avoid model load on player local server
		if( !evt.getWorld().isRemote || modelLoaded )
			return;
		
		final MCWBClient mod = MCWBClient.MOD;
		
		// Call load for all subscribers
		mod.meshLoadSubscribers.forEach(
			sub -> {
				// Throwing an exception on world load could jam the world load and print a lot of \
				// error messages that can barely help with debug. Hence we generally want to \
				// avoid any exception being thrown out here especially when you think of that \
				// #onModelLoad method could be overridden by the pack makers who do not know this.
				try { sub.onMeshLoad(); }
				catch( Exception e ) { LOGGER.except( e, "mcwb.exception_call_model_load", sub ); }
			}
		);
		
		// Clear resources after model load
		mod.meshLoadSubscribers.clear();
		mod.modelPool.clear(); // TODO: check if this is needed
		mod.meshPool.clear();
		modelLoaded = true;
	}
	
	@SubscribeEvent
	public static void onGameOverlayRender( RenderGameOverlayEvent.Pre evt )
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
	public static void onHandRender( RenderHandEvent evt ) {
		evt.setCanceled( PlayerPatchClient.instance.onHandRender() );
	}
	
	/**
	 * TODO: proper intro
	 */
	@SubscribeEvent
	public static void onSpecificHandRender( RenderSpecificHandEvent evt ) {
		evt.setCanceled( PlayerPatchClient.instance.onSpecificHandRender( evt.getHand() ) );
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
	
	/**
	 * @see InputHandler
	 */
	@SubscribeEvent
	public static void onInput( InputEvent evt )
	{
		// Avoid key input if yet not entered a world or a GUI is activated
		final Minecraft mc = MCWBClient.MC;
		if( mc.player == null || mc.currentScreen != null )
			return;
		
		// Update key states
		InputHandler.GLOBAL_KEYS.forEach( IKeyBind::update );
		final boolean co = InputHandler.CO.down;
		( co ? InputHandler.CO_KEYS : InputHandler.INCO_KEYS ).forEach( IKeyBind::update );
		( co ? InputHandler.INCO_KEYS : InputHandler.CO_KEYS ).forEach( IKeyBind::reset );
	}
	
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
		if( MCWB.MODID.equals( evt.getModID() ) )
			ConfigManager.sync( MCWB.MODID, Config.Type.INSTANCE );
	}
	
	/** for test */
	private static Mesh mesh = null;
	@SubscribeEvent
	public static void onPlayerRender( RenderPlayerEvent.Pre evt )
	{
		try
		{
			mesh = new ObjMeshBuilder().load( "models/test/marlin 1895-thumper v2_1.obj" ).quickBuild();
			mesh = MCWBClient.MOD.loadMesh( "models/fn_mk20_ssr.obj", b -> b );
		}
		catch( Exception e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		final ResourceLocation texture = new ResourceLocation( MCWB.MODID, "textures/" +
//			"0x00ff00.png"
//			"debug_box.png"
//			"marlin 1895-thumper v2.png"
			"fn_mk20_ssr.png"
		);
		MCWBClient.MC.renderEngine.bindTexture( texture );
		
		GL11.glPushMatrix();
		{
			final float s = 1F; //3F * 16F;
			GL11.glScalef( s, s, s );
			
			mesh.render();
//			FNMK20SSR.INSTANCE.render();
			
//			GlStateManager.disableCull();
//			mesh.render();
		}
		GL11.glPopMatrix();
	}
	
	@SubscribeEvent
	public static void onClientTick( ClientTickEvent evt )
	{
		// TODO: call test stuff
	}
	/** for test */
}
