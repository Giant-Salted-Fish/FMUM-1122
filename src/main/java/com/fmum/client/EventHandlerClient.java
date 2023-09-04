package com.fmum.client;

import com.fmum.client.input.KeyBindManager;
import com.fmum.client.player.PlayerPatchClient;
import com.fmum.common.FMUM;
import com.fmum.common.item.IItemType;
import com.fmum.common.pack.IContentPackFactory.IMeshLoadContext;
import com.fmum.util.GLUtil;
import com.fmum.util.Mat4f;
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
import java.util.LinkedList;
import java.util.function.Consumer;

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
	
	private static GuiScreen prev_gui = null;
	@SubscribeEvent
	static void onGUIOpen( GuiOpenEvent evt )
	{
		final GuiScreen gui = evt.getGui();
		final GameSettings settings = FMUMClient.MC.gameSettings;
		
		// Show restore vanilla key bindings if control GUI is activated.
		if ( gui instanceof GuiControls )
		{
			KeyBindManager.restoreVanillaKeyBind();
		}
		else if ( prev_gui instanceof GuiControls )
		{
			KeyBindManager.clearVanillaKeyBind(
				FMUMClient.MOD._keyBindSettingFile() );
		}
		
		else if ( gui instanceof GuiVideoSettings )
		{
			settings.viewBobbing = ori_view_bobbing;
			// TODO: gamma setting
		}
		else if ( prev_gui instanceof GuiVideoSettings )
		{
			ori_view_bobbing = settings.viewBobbing;
		}
		
		prev_gui = gui;
	}
	
	@SubscribeEvent
	static void onModelRegister( ModelRegistryEvent evt )
	{
		FMUM.MOD.logInfo( "fmum.on_model_regis" );
		
		final Collection< IItemType > items = IItemType.REGISTRY.values();
		items.forEach( it -> it.onModelRegister( evt ) );
		
		FMUM.MOD.logInfo( "fmum.model_regis_complete", items.size() );
	}
	
	@SubscribeEvent
	static void onRenderGameOverlay$Pre( RenderGameOverlayEvent.Pre evt )
	{
		if ( evt.getType() == ElementType.CROSSHAIRS ) {
			evt.setCanceled( PlayerPatchClient.instance.shouldHideCrosshair() );
		}
	}
	
	@SubscribeEvent
	static void onRenderHand( RenderHandEvent evt )
	{
		final boolean cancel_evt = PlayerPatchClient.instance.onRenderHand();
		evt.setCanceled( cancel_evt );
	}
	
	@SubscribeEvent
	static void onRenderSpecificHand( RenderSpecificHandEvent evt )
	{
		final boolean cancel_evt = PlayerPatchClient
			.instance.onRenderSpecificHand( evt.getHand() );
		evt.setCanceled( cancel_evt );
	}
	
	@SubscribeEvent
	static void onCameraSetup( CameraSetup evt )
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
//	static void onFOVModify( FOVModifier evt )
//	{
//
//	}
	
	@SubscribeEvent
	static void onFOVUpdate( FOVUpdateEvent evt )
	{
		// Disable dynamic FOV.
		evt.setNewfov( 1.0F );
	}
	
	@SubscribeEvent
	static void onMouseInput( MouseEvent evt )
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
	static void onConfigChanged( OnConfigChangedEvent evt )
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
