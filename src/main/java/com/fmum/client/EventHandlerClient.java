package com.fmum.client;

import com.fmum.client.input.KeyBindManager;
import com.fmum.client.player.PlayerPatchClient;
import com.fmum.common.FMUM;
import com.fmum.common.item.IItemType;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
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
	private EventHandlerClient() { }
	
	@SubscribeEvent
	static void onModelRegister( ModelRegistryEvent evt )
	{
		FMUM.MOD.logInfo( "fmum.on_model_regis" );
		
		final Collection< IItemType > items = IItemType.REGISTRY.values();
		items.forEach( it -> it.onModelRegister( evt ) );
		
		FMUM.MOD.logInfo( "fmum.model_regis_complete", items.size() );
	}
	
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
//			settings.mouseSensitivity = ori_mouse_sensi;
		}
		else if ( prev_gui instanceof GuiControls )
		{
			KeyBindManager.clearVanillaKeyBind(
				FMUMClient.MOD._keyBindSettingFile() );
//			ori_mouse_sensi = settings.mouseSensitivity;
		}
		
		prev_gui = gui;
	}
	
	// Disable dynamic FOV.
	@SubscribeEvent
	public static void onFOVUpdate( FOVUpdateEvent evt ) {
		evt.setNewfov( 1.0F );
	}
	
	@SubscribeEvent
	public static void onConfigChanged( OnConfigChangedEvent evt )
	{
		final boolean is_fmum_config = evt.getModID().equals( FMUM.MODID );
		if ( is_fmum_config )
		{
			ConfigManager.sync( FMUM.MODID, Config.Type.INSTANCE );
//			PlayerPatchClient.updateMouseHelperStrategy( ModConfigClient.useFlanCompatibleMouseHelper );
		}
	}
}
