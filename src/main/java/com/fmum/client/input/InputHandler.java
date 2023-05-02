package com.fmum.client.input;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.fmum.client.FMUMClient;
import com.fmum.common.IAutowireLogger;
import com.fmum.common.FMUM;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.MouseInputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Class that manages all keys in {@link FMUM}. Key bindings will be set to
 * {@link Keyboard#KEY_NONE} during game to avoid key binding conflict. They will be set back when
 * settings GUI is launched.
 * 
 * @see IKeyBind
 * @author Giant_Salted_Fish
 */
@SideOnly( Side.CLIENT )
@EventBusSubscriber( modid = FMUMClient.MODID, value = Side.CLIENT )
public final class InputHandler
{
	/**
	 * Keys that will always update.
	 */
	public static final HashSet< IKeyBind > GLOBAL_KEYS = new HashSet<>();
	
	/**
	 * Keys that will update when {@link #CO} is pressed.
	 */
	public static final HashSet< IKeyBind > CO_KEYS = new HashSet<>();
	
	/**
	 * Keys that will update when {@link #CO} is not pressed.
	 */
	public static final HashSet< IKeyBind > INCO_KEYS = new HashSet<>();
	
	static
	{
		// Need this to make sure key bindings are loaded at proper time.
		try { Class.forName( "com.fmum.client.input.Key" ); }
		catch ( Exception e ) { throw new RuntimeException( e ); }
	}
	
	private static final HashMultimap< Integer, IKeyBind > GLOBAL_MAPPER = HashMultimap.create();
	
	private static final HashMultimap< Integer, IKeyBind > CO_MAPPER = HashMultimap.create();
	
	private static final HashMultimap< Integer, IKeyBind > INCO_MAPPER = HashMultimap.create();
	
//	static
//	{
//		new VanillaKeyBindProxy(
//			"swap_hand",
//			FMUMClient.MOD,
//			Category.OTHER,
//			FMUMClient.SETTINGS.keyBindSwapHands
//		) {
//			@Override
//			protected void onAction() { PlayerPatchClient.instance.trySwapHand(); }
//		};
//	}
	
	private static final IAutowireLogger LOGGER = FMUMClient.MOD;
	
	private InputHandler() { }
	
	@SubscribeEvent
	public static void onKeyInput( KeyInputEvent evt )
	{
		// TODO: check #player
		if ( FMUMClient.MC.currentScreen != null ) { return; }
		
		final int keyCode = Keyboard.getEventKey();
		final int charCode = Keyboard.getEventCharacter();
		final int key = keyCode == 0 ? charCode + 256 : keyCode;
		final boolean state = Keyboard.getEventKeyState();
		
		GLOBAL_MAPPER.get( key ).forEach( kb -> kb.update( state ) );
		( Key.ASSIST.down ? CO_MAPPER : INCO_MAPPER ).get( key )
			.forEach( kb -> kb.update( state ) );
		( Key.ASSIST.down ? INCO_MAPPER : CO_MAPPER ).get( key )
			.forEach( kb -> kb.inactiveUpdate( state ) );
	}
	
	@SubscribeEvent
	public static void onMouseInput( MouseInputEvent evt )
	{
		if ( FMUMClient.MC.currentScreen != null ) { return; }
		
		final int button = Mouse.getEventButton() - 100;
		final boolean state = Mouse.getEventButtonState();
		
		GLOBAL_MAPPER.get( button ).forEach( kb -> kb.update( state ) );
		( Key.ASSIST.down ? CO_MAPPER : INCO_MAPPER ).get( button )
			.forEach( kb -> kb.update( state ) );
		( Key.ASSIST.down ? INCO_MAPPER : CO_MAPPER ).get( button )
			.forEach( kb -> kb.inactiveUpdate( state ) );
	}
	
	private static KeyBind prevAimKey;
	public static void restoreMcKeyBinds()
	{
		prevAimKey = Key.AIM_HOLD.keyCode != Keyboard.KEY_NONE ? Key.AIM_HOLD : Key.AIM_TOGGLE;
		IKeyBind.REGISTRY.values().forEach( IKeyBind::restoreMcKeyBind );
	}
	
	public static void clearKeyMcBinds( File file )
	{
		boolean changed = false;
		for ( IKeyBind key : IKeyBind.REGISTRY.values() ) {
			changed |= key.clearMcKeyBind();
		}
		
		// Make sure there is only one aim key bounden.
		final int none = Keyboard.KEY_NONE;
		if ( Key.AIM_HOLD.keyCode != none && Key.AIM_TOGGLE.keyCode != none )
		{
			prevAimKey.setKeyCode( none );
			changed = true;
		}
		
		KeyBinding.resetKeyBindingArrayAndHash();
		
		// If key bind has changed then save it.
		if ( changed )
		{
			updateMappers();
			saveTo( file );
		}
	}
	
	/**
	 * Save key bind settings into given ".json" file.
	 */
	public static void saveTo( File file )
	{
		try ( FileWriter out = new FileWriter( file ) )
		{
			final HashMap< String, Integer > mapper = new HashMap<>();
			IKeyBind.REGISTRY.values().forEach( key -> mapper.put( key.name(), key.keyCode() ) );
			out.write( FMUM.GSON.toJson( mapper ) );
		}
		catch ( IOException e ) { LOGGER.logException( e, "fmum.error_saving_key_binds" ); }
	}
	
	/**
	 * Read key bind settings from the given ".json" file.
	 */
	public static void readFrom( File file )
	{
		try ( FileReader in = new FileReader( file ) )
		{
			final JsonObject obj = FMUM.GSON.fromJson( in, JsonObject.class );
			obj.entrySet().forEach( e -> {
				try { IKeyBind.REGISTRY.get( e.getKey() ).setKeyCode( e.getValue().getAsInt() ); }
				catch ( NullPointerException ee ) {
					LOGGER.logError( "fmum.unrecognized_key_bind", e.getKey() );
				}
				catch ( Exception ee ) {
					LOGGER.logError( "fmum.key_code_format_error", e.toString() );
				}
			} );
		}
		catch ( IOException e ) { LOGGER.logException( e, "fmum.error_reading_key_binds" ); }
		
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
		group.forEach( kb -> mapper.put( kb.keyCode(), kb ) );
	}
}
