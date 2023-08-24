package com.fmum.client.input;

import com.fmum.client.FMUMClient;
import com.fmum.client.input.KeyBind.BindingState;
import com.google.common.collect.HashMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.MouseInputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

@SideOnly( Side.CLIENT )
@EventBusSubscriber( modid = FMUMClient.MODID, value = Side.CLIENT )
public final class KeyBindManager
{
	private static final HashMultimap< Integer, KeyBind >
		UPDATE_TABLE = HashMultimap.create();
	
	private static final Gson GSON;
	static
	{
		final GsonBuilder builder =  new GsonBuilder();
		builder.setLenient();
		builder.setPrettyPrinting();
		GSON = builder.create();
	}
	
	@SubscribeEvent
	static void onKeyboardInput( KeyInputEvent evt )
	{
		final int key_code = Keyboard.getEventKey();
		final int char_code = Keyboard.getEventCharacter();
		final int key = key_code == 0 ? char_code + 256 : key_code;
		final boolean is_down = Keyboard.getEventKeyState();
		UPDATE_TABLE.get( key ).forEach( kb -> kb.update( is_down ) );
	}
	
	@SubscribeEvent
	static void onMouseInput( MouseInputEvent evt )
	{
		final int button = Mouse.getEventButton() - 100;
		final boolean is_down = Mouse.getEventButtonState();
		UPDATE_TABLE.get( button ).forEach( kb -> kb.update( is_down ) );
	}
	
	public static void restoreVanillaKeyBind()
	{
		KeyBind.REGISTRY.values().forEach( KeyBind::restoreVanillaKeyBind );
	}
	
	public static void clearVanillaKeyBind( File file )
	{
		boolean is_changed = false;
		for ( KeyBind kb : KeyBind.REGISTRY.values() ) {
			is_changed |= kb.clearVanillaKeyBind() == BindingState.CHANGED;
		}
		
		KeyBinding.resetKeyBindingArrayAndHash();
		
		if ( is_changed )
		{
			__updateMappingTable();
			saveSettingsTo( file );
		}
	}
	
	public static void saveSettingsTo( File file )
	{
		try ( FileWriter out = new FileWriter( file ) )
		{
			final HashMap< String, String > data = new HashMap<>();
			KeyBind.REGISTRY.values().forEach( kb -> {
				final String setting = kb.keyCode() + "+" + kb.keyModifier();
				data.put( kb.identifier(), setting );
			} );
			out.write( GSON.toJson( data ) );
		}
		catch ( IOException e ) {
			FMUMClient.MOD.logException( e, "fmum.error_saving_key_binds" );
		}
		
		// Save keys will be called in two cases:
		//     1. Key binding changed by user.
		//     2. First time entering the game.
		// And for both cases it is needed to update mapping table.
		__updateMappingTable();
	}
	
	public static void loadSettingsFrom( File file )
	{
		try ( FileReader in = new FileReader( file ) )
		{
			final JsonObject data = GSON.fromJson( in, JsonObject.class );
			data.entrySet().forEach( KeyBindManager::__loadKeyBindSettingFrom );
		}
		catch ( IOException e ) {
			FMUMClient.MOD.logException( e, "fmum.key_code_format_error" );
		}
		
		__updateMappingTable();
	}
	
	private static void __loadKeyBindSettingFrom( Entry< String, JsonElement > e )
	{
		try
		{
			final KeyBind kb = KeyBind.REGISTRY.get( e.getKey() );
			final String[] setting = e.getValue().getAsString().split( "\\+" );
			final int key_code = Integer.parseInt( setting[0] );
			final KeyModifier modifier = KeyModifier.valueFromString( setting[1] );
			kb.setKeyCodeAndModifier( key_code, modifier );
		}
		catch ( NullPointerException e_ ) {
			FMUMClient.MOD.logError( "fmum.unrecognized_key_bind", e.getKey() );
		}
		catch ( IllegalArgumentException e_ ) {
			FMUMClient.MOD.logError( "fmum.key_code_format_error", e.toString() );
		}
	}
	
	private static void __updateMappingTable()
	{
		UPDATE_TABLE.clear();
		KeyBind.REGISTRY.values().forEach(
			kb -> UPDATE_TABLE.put( kb.keyCode(), kb ) );
	}
}
