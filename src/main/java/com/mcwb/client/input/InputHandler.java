package com.mcwb.client.input;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.TreeSet;

import org.lwjgl.input.Keyboard;

import com.google.gson.JsonObject;
import com.mcwb.client.MCWBClient;
import com.mcwb.client.input.Key.KeyCategory;
import com.mcwb.common.IAutowireLogger;
import com.mcwb.common.MCWB;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Class that manages all keys in {@link MCWB}. Key bindings will be set to
 * {@link Keyboard#KEY_NONE} during game to avoid key binding conflict. They will be set back when
 * settings GUI is launched.
 * 
 * @see Key
 * @see KeyBindType
 * @author Giant_Salted_Fish
 */
@SideOnly( Side.CLIENT )
public final class InputHandler
{
	// TODO: can be better by directly map key codes to updater
	
	/**
	 * Keys that will always update
	 */
	public static final TreeSet< IKeyBind > GLOBAL_KEYS = new TreeSet<>();
	
	/**
	 * Keys that will update when {@link #CO} is pressed
	 */
	public static final TreeSet< IKeyBind > CO_KEYS = new TreeSet<>();
	
	/**
	 * Keys that will update when {@link #CO} is not pressed
	 */
	public static final TreeSet< IKeyBind > INCO_KEYS = new TreeSet<>();
	
	/**
	 * These keys always update
	 */
	public static final KeyBind
		PULL_TRIGGER = new KeyBind( Key.PULL_TRIGGER, KeyCategory.GUN, 0 - 100, GLOBAL_KEYS ),
		AIM_HOLD = new KeyBind( Key.AIM_HOLD, KeyCategory.GUN, 1 - 100, GLOBAL_KEYS ),
		AIM_TOGGLE = new KeyBind( Key.AIM_TOGGLE, KeyCategory.GUN, Keyboard.KEY_NONE, GLOBAL_KEYS ),
		RELOAD = new KeyBind( Key.RELOAD, KeyCategory.GUN, Keyboard.KEY_R ),
		LOAD_UNLOAD_MAG = new KeyBind( Key.LOAD_UNLOAD_MAG, KeyCategory.GUN, Keyboard.KEY_T ),
		
		// TODO: change the default bind key for this maybe
		SELECT_TOGGLE = new KeyBind( Key.SELECT_TOGGLE, KeyCategory.MODIFY, Keyboard.KEY_V ),
		SELECT_UP = new KeyBind( Key.SELECT_UP, KeyCategory.MODIFY, Keyboard.KEY_UP ),
		SELECT_DOWN = new KeyBind( Key.SELECT_DOWN, KeyCategory.MODIFY, Keyboard.KEY_DOWN ),
		SELECT_LEFT = new KeyBind( Key.SELECT_LEFT, KeyCategory.MODIFY, Keyboard.KEY_LEFT ),
		SELECT_RIGHT = new KeyBind( Key.SELECT_RIGHT, KeyCategory.MODIFY, Keyboard.KEY_RIGHT ),
		SELECT_CONFIRM = new KeyBind( Key.SELECT_CONFIRM, KeyCategory.MODIFY, Keyboard.KEY_G ),
		SELECT_CANCEL = new KeyBind( Key.SELECT_CANCEL, KeyCategory.MODIFY, Keyboard.KEY_H ),
		
		CO = new KeyBind( Key.CO, KeyCategory.ASSIST, Keyboard.KEY_Z, GLOBAL_KEYS );
	
	/**
	 * These keys will update if {@link #CO} is not down
	 */
	public static final KeyBind
		FREE_VIEW = new KeyBind( Key.FREE_VIEW, KeyCategory.GENERAL, Keyboard.KEY_LMENU ),
		TOGGLE_MODIFY = new KeyBind( Key.TOGGLE_MODIFY, KeyCategory.GUN, Keyboard.KEY_I );
	
	/**
	 * These keys will update if {@link #CO} is down
	 */
	public static final KeyBind
		CO_FREE_VIEW = new KeyBind( Key.CO_FREE_VIEW, KeyCategory.ASSIST, Keyboard.KEY_NONE ),
		CO_RELOAD = new KeyBind( Key.CO_RELOAD, KeyCategory.ASSIST, Keyboard.KEY_NONE ),
		CO_LOAD_UNLOAD_MAG = new KeyBind(
			Key.CO_LOAD_UNLOAD_MAG, KeyCategory.ASSIST, Keyboard.KEY_NONE ),
		CO_TOGGLE_MODIFY = new KeyBind(
			Key.CO_TOGGLE_MODIFY, KeyCategory.ASSIST, Keyboard.KEY_NONE );
	
	private static final IAutowireLogger LOGGER = MCWBClient.MOD;
	
	private InputHandler() { }
	
	private static KeyBind prevAimKey;
	public static void restoreMcKeyBinds()
	{
		prevAimKey = AIM_HOLD.keyCode != Keyboard.KEY_NONE ? AIM_HOLD : AIM_TOGGLE;
		IKeyBind.REGISTRY.values().forEach( IKeyBind::restoreMcKeyBind );
	}
	
	public static void clearKeyMcBinds( File file )
	{
		boolean changed = false;
		for( IKeyBind key : IKeyBind.REGISTRY.values() )
			changed |= key.clearMcKeyBind();
		
		// Make sure there is only one aim key bounden
		final int none = Keyboard.KEY_NONE;
		if( AIM_HOLD.keyCode != none && AIM_TOGGLE.keyCode != none )
		{
			prevAimKey.$keyCode( none );
			changed = true;
		}
		
		KeyBinding.resetKeyBindingArrayAndHash();
		
		// If key bind has changed then save it
		if( changed )
			saveTo( file );
	}
	
	/**
	 * Save key bind settings into given ".json" file
	 */
	public static void saveTo( File file )
	{
		try( FileWriter out = new FileWriter( file ) )
		{
			final HashMap< String, Integer > mapper = new HashMap<>();
			IKeyBind.REGISTRY.values().forEach( key -> mapper.put( key.name(), key.keyCode() ) );
			out.write( MCWB.GSON.toJson( mapper ) );
		}
		catch( IOException e ) { LOGGER.except( e, "mcwb.error_saving_key_binds" ); }
	}
	
	/**
	 * Read key bind settings from the given ".json" file
	 */
	public static void readFrom( File file )
	{
		try( FileReader in = new FileReader( file ) )
		{
			final JsonObject obj = MCWB.GSON.fromJson( in, JsonObject.class );
			obj.entrySet().forEach( e -> {
				try { IKeyBind.REGISTRY.get( e.getKey() ).$keyCode( e.getValue().getAsInt() ); }
				catch( NullPointerException ee ) {
					LOGGER.error( "mcwb.unrecognized_key_bind", e.getKey() );
				}
				catch( Exception ee ) {
					LOGGER.error( "mcwb.key_code_format_error", e.toString() );
				}
			} );
		}
		catch( IOException e ) { LOGGER.except( e, "mcwb.error_reading_key_binds" ); }
	}
}
