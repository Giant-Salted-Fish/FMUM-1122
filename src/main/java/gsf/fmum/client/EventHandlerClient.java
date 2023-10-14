package gsf.fmum.client;

import gsf.fmum.client.input.GuiControlsProxy;
import gsf.fmum.client.player.PlayerPatchClient;
import gsf.fmum.common.FMUM;
import gsf.fmum.common.item.IItemType;
import gsf.fmum.util.GLUtil;
import gsf.fmum.util.Mat4f;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiVideoSettings;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collection;

@SideOnly( Side.CLIENT )
@EventBusSubscriber( modid = FMUM.MODID, value = Side.CLIENT )
public final class EventHandlerClient
{
	public static boolean ori_view_bobbing;
	static
	{
		final GameSettings settings = FMUMClient.MC.gameSettings;
		ori_view_bobbing = settings.viewBobbing;
	}
	
	// Handle mesh load on first time entering the world.
	static
	{
		MinecraftForge.EVENT_BUS.register( new Object() {
			@SubscribeEvent
			void onWorldLoad( WorldEvent.Load evt )
			{
				// Only load once on logical client side.
				if ( evt.getWorld().isRemote )
				{
					FMUMClient.MOD._onMeshLoad();
					MinecraftForge.EVENT_BUS.unregister( this );
				}
			}
		} );
	}
	
	private EventHandlerClient() { }
	
	@SubscribeEvent
	static void _onGUIOpen( GuiOpenEvent evt )
	{
		final GuiScreen gui = evt.getGui();
		final GuiScreen prev_gui = FMUMClient.MC.currentScreen;
		final GameSettings settings = FMUMClient.MC.gameSettings;
		
		// Replace vanilla controls with enhanced one.
		// TODO: Add support to disable it to avoid conflict.
		if ( gui instanceof GuiControls ) {
			evt.setGui( new GuiControlsProxy( prev_gui, settings ) );
		}
		
		// TODO: This may also be accomplished by replacing the GUI.
		else if ( gui instanceof GuiVideoSettings )
		{
			settings.viewBobbing = ori_view_bobbing;
			// TODO: gamma setting
		}
		else if ( prev_gui instanceof GuiVideoSettings )
		{
			ori_view_bobbing = settings.viewBobbing;
		}
	}
	
	@SubscribeEvent
	static void _onModelRegister( ModelRegistryEvent evt )
	{
		FMUM.MOD.logInfo( "fmum.on_model_regis" );
		
		final Collection< IItemType > items = IItemType.REGISTRY.values();
		items.forEach( it -> it.onModelRegister( evt ) );
		
		FMUM.MOD.logInfo( "fmum.model_regis_complete", items.size() );
	}
	
	@SubscribeEvent
	static void _onRenderGameOverlay$Pre( RenderGameOverlayEvent.Pre evt )
	{
		if ( evt.getType() == ElementType.CROSSHAIRS ) {
			evt.setCanceled( PlayerPatchClient.instance.shouldHideCrosshair() );
		}
	}
	
	@SubscribeEvent
	static void _onRenderHand( RenderHandEvent evt )
	{
		final boolean cancel_evt = PlayerPatchClient.instance.onRenderHand();
		evt.setCanceled( cancel_evt );
	}
	
	@SubscribeEvent
	static void _onRenderSpecificHand( RenderSpecificHandEvent evt )
	{
		final boolean cancel_evt = PlayerPatchClient
			.instance.onRenderSpecificHand( evt.getHand() );
		evt.setCanceled( cancel_evt );
	}
	
	@SubscribeEvent
	static void _onCameraSetup( CameraSetup evt )
	{
		evt.setYaw( 0.0F );
		evt.setPitch( 0.0F );
		evt.setRoll( 0.0F );
		
		final Mat4f mat = Mat4f.locate();
		PlayerPatchClient.instance.camera.getViewMat( mat );
		GLUtil.glMulMatrix( mat );
		mat.release();
	}
	
	// TODO: Apply FOV modification here for scope glass texture rendering.
//	@SubscribeEvent
//	static void _onFOVModify( FOVModifier evt )
//	{
//
//	}
	
	@SubscribeEvent
	static void _onFOVUpdate( FOVUpdateEvent evt )
	{
		// Disable dynamic FOV.
		evt.setNewfov( 1.0F );
	}
	
	@SubscribeEvent
	static void _onMouseInput( MouseEvent evt )
	{
		final int dwheel = evt.getDwheel();
		if ( dwheel != 0 )
		{
			final boolean cancel_evt = PlayerPatchClient
				.instance.onMouseWheelInput( dwheel );
			evt.setCanceled( cancel_evt );
		}
	}
	
	@SubscribeEvent
	static void _onConfigChanged( OnConfigChangedEvent evt )
	{
		final boolean is_fmum_config = evt.getModID().equals( FMUM.MODID );
		if ( is_fmum_config )
		{
			ConfigManager.sync( FMUM.MODID, Config.Type.INSTANCE );
			PlayerPatchClient.setMouseHelperStrategy(
				ModConfigClient.use_flan_compatible_mousehelper );
		}
	}
}
