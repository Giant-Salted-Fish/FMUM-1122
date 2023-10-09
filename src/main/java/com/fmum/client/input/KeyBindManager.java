package com.fmum.client.input;

import com.fmum.client.FMUMClient;
import com.fmum.client.input.IKeyBind.ClearState;
import com.fmum.client.input.IKeyBind.UpdateResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.client.settings.KeyBinding;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@SideOnly( Side.CLIENT )
@EventBusSubscriber( modid = FMUMClient.MODID, value = Side.CLIENT )
public final class KeyBindManager
{
	private static final HashMap< Integer, List< IKeyBind > >
		UPDATE_TABLE = new HashMap<>();
	
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
		for ( IKeyBind kb : UPDATE_TABLE.getOrDefault( key, Collections.emptyList() ) )
		{
			final UpdateResult result = kb.update( is_down );
			if ( result == UpdateResult.CONSUMED ) {
				break;
			}
		}
	}
	
	@SubscribeEvent
	static void onMouseInput( MouseInputEvent evt )
	{
		final int button = Mouse.getEventButton() - 100;
		final boolean is_down = Mouse.getEventButtonState();
		for ( IKeyBind kb : UPDATE_TABLE.getOrDefault( button, Collections.emptyList() ) )
		{
			final UpdateResult result = kb.update( is_down );
			if ( result == UpdateResult.CONSUMED ) {
				break;
			}
		}
	}
	
	public static void restoreVanillaKeyBind()
	{
		IKeyBind.REGISTRY.values().forEach( IKeyBind::restoreVanillaKeyBind );
	}
	
	public static void clearVanillaKeyBind( File file )
	{
		boolean is_key_bind_changed = false;
		for ( IKeyBind kb : IKeyBind.REGISTRY.values() )
		{
			final ClearState state = kb.clearVanillaKeyBind();
			is_key_bind_changed |= state == ClearState.CHANGED;
		}
		
		KeyBinding.resetKeyBindingArrayAndHash();
		
		if ( is_key_bind_changed )
		{
			__updateMappingTable();
			saveSettingsTo( file );
		}
	}
	
	public static void saveSettingsTo( File file )
	{
		try ( FileWriter out = new FileWriter( file ) )
		{
			final HashMap< String, Object > data = new HashMap<>();
			IKeyBind.REGISTRY.values().forEach(
				kb -> data.put( kb.identifier(), kb.serialize() ) );
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
			data.entrySet().forEach( e -> {
				final String id = e.getKey();
				final Optional< IKeyBind > kb = IKeyBind.REGISTRY.lookup( id );
				if ( kb.isPresent() )
				{
					try {
						kb.get().deserialize( e.getValue() );
					}
					catch ( Exception e_ )
					{
						final String err_msg = "fmum.error_reading_key_bind_setting";
						FMUMClient.MOD.logError( err_msg, id );
					}
				}
				else
				{
					final String err_msg = "fmum.unrecognized_key_bind";
					FMUMClient.MOD.logError( err_msg, id );
				}
			} );
		}
		catch ( IOException e )
		{
			final String err_msg = "fmum.error_reading_key_binds";
			FMUMClient.MOD.logException( e, err_msg );
		}
		
		__updateMappingTable();
	}
	
	private static void __updateMappingTable()
	{
		UPDATE_TABLE.clear();
		IKeyBind.REGISTRY.values().forEach(
			kb -> UPDATE_TABLE.compute( kb.keyCode(), ( code, list ) -> {
				if ( list == null ) {
					list = new ArrayList<>();
				}
				list.add( kb );
				return list;
			} )
		);
		
		final Comparator< IKeyBind > cmp =
			Comparator.comparingInt( IKeyBind::priority );
		UPDATE_TABLE.values().forEach( arr -> arr.sort( cmp ) );
	}
}
