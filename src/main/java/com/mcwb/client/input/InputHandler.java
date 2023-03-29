package com.mcwb.client.input;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import com.mcwb.client.MCWBClient;
import com.mcwb.client.input.Key.Category;
import com.mcwb.common.IAutowireLogger;
import com.mcwb.common.MCWB;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.MouseInputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Class that manages all keys in {@link MCWB}. Key bindings will be set to
 * {@link Keyboard#KEY_NONE} during game to avoid key binding conflict. They will be set back when
 * settings GUI is launched.
 * 
 * @see Key
 * @see IKeyBind
 * @author Giant_Salted_Fish
 */
@SideOnly( Side.CLIENT )
@EventBusSubscriber( modid = MCWBClient.ID, value = Side.CLIENT )
public final class InputHandler
{
	/**
	 * Keys that will always update
	 */
	public static final HashSet< IKeyBind > GLOBAL_KEYS = new HashSet<>();
	
	/**
	 * Keys that will update when {@link #CO} is pressed
	 */
	public static final HashSet< IKeyBind > CO_KEYS = new HashSet<>();
	
	/**
	 * Keys that will update when {@link #CO} is not pressed
	 */
	public static final HashSet< IKeyBind > INCO_KEYS = new HashSet<>();
	
	/**
	 * These keys always update
	 */
	public static final KeyBind
		PULL_TRIGGER = new KeyBind( Key.PULL_TRIGGER, Category.GUN, 0 - 100, GLOBAL_KEYS ),
		AIM_HOLD = new KeyBind( Key.AIM_HOLD, Category.GUN, 1 - 100, GLOBAL_KEYS ),
		AIM_TOGGLE = new KeyBind( Key.AIM_TOGGLE, Category.GUN, Keyboard.KEY_NONE, GLOBAL_KEYS ),
		
		// TODO: change the default bind key for this maybe
		SELECT_TOGGLE = new KeyBind( Key.SELECT_TOGGLE, Category.MODIFY, Keyboard.KEY_V ),
		SELECT_UP = new KeyBind( Key.SELECT_UP, Category.MODIFY, Keyboard.KEY_UP ),
		SELECT_DOWN = new KeyBind( Key.SELECT_DOWN, Category.MODIFY, Keyboard.KEY_DOWN ),
		SELECT_LEFT = new KeyBind( Key.SELECT_LEFT, Category.MODIFY, Keyboard.KEY_LEFT ),
		SELECT_RIGHT = new KeyBind( Key.SELECT_RIGHT, Category.MODIFY, Keyboard.KEY_RIGHT ),
		SELECT_CONFIRM = new KeyBind( Key.SELECT_CONFIRM, Category.MODIFY, Keyboard.KEY_G ),
		SELECT_CANCEL = new KeyBind( Key.SELECT_CANCEL, Category.MODIFY, Keyboard.KEY_H ),
		
		CO = new KeyBind( Key.CO, Category.ASSIST, Keyboard.KEY_Z, GLOBAL_KEYS );
	
	/**
	 * These keys will update if {@link #CO} is not down
	 */
	public static final KeyBind
		FREE_VIEW = new KeyBind( Key.FREE_VIEW, Category.GENERAL, Keyboard.KEY_LMENU ),
		TOGGLE_MODIFY = new KeyBind( Key.TOGGLE_MODIFY, Category.GUN, Keyboard.KEY_I ),
		
		RELOAD = new KeyBind( Key.RELOAD, Category.GUN, Keyboard.KEY_R ),
		LOAD_UNLOAD_MAG = new KeyBind( Key.LOAD_UNLOAD_MAG, Category.GUN, Keyboard.KEY_T ),
		INSPECT = new KeyBind( Key.INSPECT, Category.GUN, Keyboard.KEY_V );
	
	/**
	 * These keys will update if {@link #CO} is down
	 */
	public static final KeyBind
		CO_FREE_VIEW = new KeyBind( Key.CO_FREE_VIEW, Category.ASSIST, Keyboard.KEY_NONE ),
		CO_RELOAD = new KeyBind( Key.CO_RELOAD, Category.ASSIST, Keyboard.KEY_NONE ),
		CO_LOAD_UNLOAD_MAG = new KeyBind( Key.CO_LOAD_UNLOAD_MAG, Category.ASSIST, Keyboard.KEY_NONE ),
		CO_INSPECT = new KeyBind( Key.CO_INSPECT, Category.ASSIST, Keyboard.KEY_NONE ),
		CO_TOGGLE_MODIFY = new KeyBind( Key.CO_TOGGLE_MODIFY, Category.ASSIST, Keyboard.KEY_NONE );
	
	private static final HashMultimap< Integer, IKeyBind > GLOBAL_MAPPER = HashMultimap.create();
	
	private static final HashMultimap< Integer, IKeyBind > CO_MAPPER = HashMultimap.create();
	
	private static final HashMultimap< Integer, IKeyBind > INCO_MAPPER = HashMultimap.create();
	
//	static
//	{
//		new VanillaKeyBindProxy(
//			"swap_hand",
//			MCWBClient.MOD,
//			Category.OTHER,
//			MCWBClient.SETTINGS.keyBindSwapHands
//		) {
//			@Override
//			protected void onAction() { PlayerPatchClient.instance.trySwapHand(); }
//		};
//	}
	
	private static final IAutowireLogger LOGGER = MCWBClient.MOD;
	
	private InputHandler() { }
	
	@SubscribeEvent
	public static void onKeyInput( KeyInputEvent evt )
	{
		// TODO: check #player
		if ( MCWBClient.MC.currentScreen != null ) return;
		
		final int keyCode = Keyboard.getEventKey();
		final int charCode = Keyboard.getEventCharacter();
		final int key = keyCode == 0 ? charCode + 256 : keyCode;
		final boolean state = Keyboard.getEventKeyState();
		GLOBAL_MAPPER.get( key ).forEach( kb -> kb.update( state ) );
		( CO.down ? CO_MAPPER : INCO_MAPPER ).get( key ).forEach( kb -> kb.update( state ) );
		( CO.down ? INCO_MAPPER : CO_MAPPER ).get( key )
			.forEach( kb -> kb.inactiveUpdate( state ) );
	}
	
	@SubscribeEvent
	public static void onMouseInput( MouseInputEvent evt )
	{
		if ( MCWBClient.MC.currentScreen != null ) return;
		
		final int button = Mouse.getEventButton() - 100;
		final boolean state = Mouse.getEventButtonState();
		GLOBAL_MAPPER.get( button ).forEach( kb -> kb.update( state ) );
		( CO.down ? CO_MAPPER : INCO_MAPPER ).get( button ).forEach( kb -> kb.update( state ) );
		( CO.down ? INCO_MAPPER : CO_MAPPER ).get( button )
			.forEach( kb -> kb.inactiveUpdate( state ) );
	}
	
	private static KeyBind prevAimKey;
	public static void restoreMcKeyBinds()
	{
		prevAimKey = AIM_HOLD.keyCode != Keyboard.KEY_NONE ? AIM_HOLD : AIM_TOGGLE;
		IKeyBind.REGISTRY.values().forEach( IKeyBind::restoreMcKeyBind );
	}
	
	public static void clearKeyMcBinds( File file )
	{
		boolean changed = false;
		for ( IKeyBind key : IKeyBind.REGISTRY.values() )
			changed |= key.clearMcKeyBind();
		
		// Make sure there is only one aim key bounden
		final int none = Keyboard.KEY_NONE;
		if ( AIM_HOLD.keyCode != none && AIM_TOGGLE.keyCode != none )
		{
			prevAimKey.$keyCode( none );
			changed = true;
		}
		
		KeyBinding.resetKeyBindingArrayAndHash();
		
		// If key bind has changed then save it
		if ( changed )
		{
			updateMappers();
			saveTo( file );
		}
	}
	
	/**
	 * Save key bind settings into given ".json" file
	 */
	public static void saveTo( File file )
	{
		try ( FileWriter out = new FileWriter( file ) )
		{
			final HashMap< String, Integer > mapper = new HashMap<>();
			IKeyBind.REGISTRY.values().forEach( key -> mapper.put( key.name(), key.keyCode() ) );
			out.write( MCWB.GSON.toJson( mapper ) );
		}
		catch ( IOException e ) { LOGGER.except( e, "mcwb.error_saving_key_binds" ); }
	}
	
	/**
	 * Read key bind settings from the given ".json" file
	 */
	public static void readFrom( File file )
	{
		try ( FileReader in = new FileReader( file ) )
		{
			final JsonObject obj = MCWB.GSON.fromJson( in, JsonObject.class );
			obj.entrySet().forEach( e -> {
				try { IKeyBind.REGISTRY.get( e.getKey() ).$keyCode( e.getValue().getAsInt() ); }
				catch ( NullPointerException ee ) {
					LOGGER.error( "mcwb.unrecognized_key_bind", e.getKey() );
				}
				catch ( Exception ee ) {
					LOGGER.error( "mcwb.key_code_format_error", e.toString() );
				}
			} );
		}
		catch ( IOException e ) { LOGGER.except( e, "mcwb.error_reading_key_binds" ); }
		
		updateMappers();
	}
	
	public static void updateMappers()
	{
		updateMapper( GLOBAL_KEYS, GLOBAL_MAPPER );
		updateMapper( CO_KEYS, CO_MAPPER );
		updateMapper( INCO_KEYS, INCO_MAPPER );
	}
	
	private static void updateMapper(
		Collection< IKeyBind > group,
		Multimap< Integer, IKeyBind > mapper
	) {
		mapper.clear();
		group.forEach( key -> {
			final int code = key.keyCode();
			if ( code != 0 ) mapper.put( code, key );
		} );
	}
}
