package com.fmum.client.input;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.TreeSet;

import javax.annotation.Nullable;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.fmum.client.FMUMClient;
import com.fmum.common.FMUM;
import com.fmum.common.Launcher.AutowireLogger;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Single instance that manages all of the keys of {@link FMUM}. Key bindings will be set to
 * {@link Keyboard#KEY_NONE} during game to avoid key binding conflict. They will be set back when
 * settings GUI is launched.
 * 
 * @see TypeKeyBind
 * @author Giant_Salted_Fish
 */
@SideOnly( Side.CLIENT )
public abstract class InputHandler
{
	/**
	 * Key categories
	 */
	public static final String
		KEY_CATEGORY_FMUM = "keycategory.fmum",
		KEY_CATEGORY_GUN = "keycategory.fmum.gun",
		KEY_CATEGORY_ASSIST = "keycategory.fmum.assist",
		KEY_CATEGORY_MODIFY = "keycategory.fmum.modification",
		KEY_CATEGORY_OTHER = "keycategory.fmum.other";
	
	/**
	 * For developer use only. Should be removed in release version.
	 */
	public static final String KEY_CATEGORY_DEV = "keycategory.fmum.test";
	
	/**
	 * Keys that will always update
	 */
	public static final TreeSet< MetaKeyBind > globalKeys = new TreeSet<>();
	
	/**
	 * Keys that will update when {@link #CO} is pressed
	 */
	public static final TreeSet< MetaKeyBind > coKeys = new TreeSet<>();
	
	/**
	 * Keys that will update when {@link #CO} is not pressed
	 */
	public static final TreeSet< MetaKeyBind > incoKeys = new TreeSet<>();
	
	/**
	 * <p> These keys will always update. </p>
	 * 
	 * <p> CATEGORY: {@link #KEY_CATEGORY_DEV}, {@link #KEY_CATEGORY_MODIFY}. </p>
	 */
	public static final TypeKeyBind
		TEST_UP = regis(
			new TypeKeyBind( "testup", Keyboard.KEY_UP, KEY_CATEGORY_DEV )
		),
		TEST_DOWN = regis(
			new TypeKeyBind( "testdown", Keyboard.KEY_DOWN, KEY_CATEGORY_DEV )
		),
		TEST_LEFT = regis(
			new TypeKeyBind( "testleft", Keyboard.KEY_LEFT, KEY_CATEGORY_DEV )
		),
		TEST_RIGHT = regis(
			new TypeKeyBind( "testright", Keyboard.KEY_RIGHT, KEY_CATEGORY_DEV )
		),
		TEST_ENTER = regis(
			new TypeKeyBind( "testenter", Keyboard.KEY_NUMPAD5, KEY_CATEGORY_DEV )
		),
		TEST_QUIT = regis(
			new TypeKeyBind( "testquit", Keyboard.KEY_NUMPAD2, KEY_CATEGORY_DEV )
		),
		DEBUG = regis(
			new TypeKeyBind( "debug", Keyboard.KEY_F10, KEY_CATEGORY_DEV )
			{
				@Override
				public void fire() {
					FMUMClient.addPromptMsg( "DEBUG mode " + ( FMUM.toggleDebug() ? "on" : "off" ) );
				}
			}
		);
	
	/**
	 * <p> These keys will update if {@link #CO} is not down. </p>
	 * 
	 * <p> CATEGORY: {@link #KEY_CATEGORY_GUN}. </p>
	 */
	public static final TypeKeyBind
		FIRE = regis(
			new TypeKeyBind( Key.FIRE, -100, KEY_CATEGORY_GUN ),
			globalKeys
		),
		AIM_HOLD = regis(
			new TypeKeyBind( Key.AIM_HOLD, -99, KEY_CATEGORY_GUN ),
			globalKeys
		),
		AIM_TOGGLE = regis(
			new TypeKeyBind( Key.AIM_TOGGLE, Keyboard.KEY_NONE, KEY_CATEGORY_GUN ),
			globalKeys
		),
		TOGGLE_MANUAL = regis(
			new TypeKeyBind( Key.TOGGLE_MANUAL, Keyboard.KEY_PERIOD, KEY_CATEGORY_GUN )
			{
				@Override
				public void fire()
				{
					FMUMClient.addPromptMsg(
						I18n.format(
							( FMUMClient.MOD.manualMode = !FMUMClient.MOD.manualMode )
							? "msg.fmum.manualmodeon"
							: "msg.fmum.manualmodeoff"
						)
					);
				};
			}
		),
		LOOK_AROUND = regis(
			new TypeKeyBind( Key.LOOK_AROUND, Keyboard.KEY_LMENU, KEY_CATEGORY_GUN )
		),
		VIEW_WEAPON = regis(
			new TypeKeyBind( Key.VIEW_WEAPON, Keyboard.KEY_V, KEY_CATEGORY_GUN )
		),
		TOGGLE_MODIFY = regis(
			new TypeKeyBind( Key.TOGGLE_MODIFY, Keyboard.KEY_I, KEY_CATEGORY_GUN )
		);
	
	/**
	 * <p> These keys will update if {@link #CO} is down. {@link #CO} itself will always update. </p>
	 * 
	 * <p> CATEGORY: {@link #KEY_CATEGORY_ASSIST}. </p>
	 */
	public static final TypeKeyBind
		CO = regis(
			new TypeKeyBind( Key.CO, Keyboard.KEY_Z, KEY_CATEGORY_ASSIST),
			globalKeys
		),
		CO_TOGGLE_MANUAL = regis(
			new TypeKeyBind( Key.CO_TOGGLE_MANUAL, Keyboard.KEY_NONE, KEY_CATEGORY_ASSIST )
			{
				@Override
				public void fire() { CO_TOGGLE_MANUAL.fire(); }
			}
		),
		CO_LOOK_ARROUND = regis(
			new TypeKeyBind( Key.CO_LOOK_AROUND, Keyboard.KEY_NONE, KEY_CATEGORY_ASSIST )
		),
		CO_VIEW_WEAPON = regis(
			new TypeKeyBind( Key.CO_VIEW_WEAPON, Keyboard.KEY_NONE, KEY_CATEGORY_ASSIST )
		),
		CO_TOGGLE_MODIFY = regis(
			new TypeKeyBind( Key.CO_TOGGLE_MODIFY, Keyboard.KEY_NONE, KEY_CATEGORY_ASSIST )
		);
	
	/**
	 * <p> These keys will always update. </p>
	 * 
	 * <p> CATEGORY: {@link #KEY_CATEGORY_MODIFY}. </p>
	 */
	public static final TypeKeyBind
		SELECT_UP = regis(
			new TypeKeyBind( Key.SELECT_UP, Keyboard.KEY_UP, KEY_CATEGORY_MODIFY )
		),
		SELECT_DOWN = regis(
			new TypeKeyBind( Key.SELECT_DOWN, Keyboard.KEY_DOWN, KEY_CATEGORY_MODIFY )
		),
		SELECT_LEFT = regis(
			new TypeKeyBind( Key.SELECT_LEFT, Keyboard.KEY_LEFT, KEY_CATEGORY_MODIFY )
		),
		SELECT_RIGHT = regis(
			new TypeKeyBind( Key.SELECT_RIGHT, Keyboard.KEY_RIGHT, KEY_CATEGORY_MODIFY )
		),
		SELECT_CONFIRM = regis(
			new TypeKeyBind( Key.SELECT_CONFIRM, Keyboard.KEY_G, KEY_CATEGORY_MODIFY )
		),
		SELECT_CANCEL = regis(
			new TypeKeyBind( Key.SELECT_CANCEL, Keyboard.KEY_H, KEY_CATEGORY_MODIFY )
		),
		SELECT_TOGGLE = regis(
			new TypeKeyBind( Key.SELECT_TOGGLE, Keyboard.KEY_V, KEY_CATEGORY_MODIFY )
		);
	
	private static final AutowireLogger log = new AutowireLogger() { };
	
	private InputHandler() { }
	
	public static void showMCKeyBind()
	{
		for( MetaKeyBind key : MetaKeyBind.regis.values() )
			key.showMCKeyBind();
	}
	
	public static void clearMCKeyBind()
	{
		boolean flag = false;
		for( MetaKeyBind key : MetaKeyBind.regis.values() )
			flag |= key.clearMCKeyBind();
		
		// TODO: validate if this is needed
		KeyBinding.resetKeyBindingArrayAndHash();
		
		if( flag )
			saveTo( FMUMClient.MOD.keyBindsFile );
	}
	
	public static void saveTo( File file )
	{
		try( BufferedWriter out = new BufferedWriter( new FileWriter( file ) ) )
		{
			for( MetaKeyBind key : MetaKeyBind.regis.values() )
			{
				out.write( key.name() + ":" + key.keyCode() );
				out.newLine();
			}
		}
		catch( IOException e ) {
			log.log().error( I18n.format( "fmum.errorsavingkeybinds" ), e );
		}
	}
	
	public static void readFrom( File file )
	{
		try( BufferedReader in = new BufferedReader( new FileReader( file ) ) )
		{
			for( String line; ( line = in.readLine() ) != null; )
			{
				final int i = line.indexOf( ':' );
				try
				{
					MetaKeyBind.regis.get( line.substring( 0, i ) ).$keyCode(
						Integer.parseInt( line.substring( i + 1 ) )
					);
				}
				catch( NullPointerException e ) {
					log.log().error( I18n.format( "fmum.keycodeformaterror", line ) );
				}
				catch( NumberFormatException e ) {
					log.log().error( I18n.format( "fmum.unrecognizedkeybind", line ) );
				}
			}
		}
		catch( IOException e ) {
			log.log().error( I18n.format( "fmum.errorreadingkeybinds" ), e );
		}
	}
	
	/**
	 * Check if key of given key code is being pressed or not. Switch mouse button if key code is
	 * negative.
	 * 
	 * @param keyCode Key code to test. Negative number means a mouse button.
	 * @return {@code true} if the corresponding key is being pressed now
	 */
	public static boolean down( int keyCode )
	{
		return(
			keyCode != Keyboard.KEY_NONE
			&& (
				keyCode < 0
				? Mouse.isButtonDown( keyCode + 100 )
				: Keyboard.isKeyDown( keyCode )
			)
		);
	}
	
	public static boolean regis( String channel, MetaKeyBind keyBind )
	{
		switch( channel )
		{
		case "GLOBAL":
		case "ALWAYS":
		case "UNIVERSAL":
			return globalKeys.add( keyBind );
		case "TRIGGER":
		case "NORMAL":
		case "DEFAULT":
		case "PRESS":
			return incoKeys.add( keyBind );
		case "CO":
		case "COKEY":
		case "ASSIST":
			return coKeys.add( keyBind );
		default:
			log.log().error(
				I18n.format(
					"fmum.keyupdategroupnotfound",
					channel,
					keyBind.toString()
				)
			);
			return false;
		}
	}
	
	private static TypeKeyBind regis( TypeKeyBind keyBind ) { return regis( keyBind, null ); }
	
	private static TypeKeyBind regis(
		TypeKeyBind keyBind,
		@Nullable Collection< MetaKeyBind > group
	) {
		// Group specified, add it to the group
		if( group != null )
			group.add( keyBind );
		
		// Otherwise, assign group based on its category
		else switch( keyBind.category() )
		{
		case KEY_CATEGORY_DEV:
		case KEY_CATEGORY_MODIFY:
			globalKeys.add( keyBind );
			break;
		case KEY_CATEGORY_ASSIST:
			coKeys.add( keyBind );
			break;
		case KEY_CATEGORY_FMUM:
		case KEY_CATEGORY_GUN:
		case KEY_CATEGORY_OTHER:
			incoKeys.add( keyBind );
			break;
		default:
			// This should never happen
			throw new RuntimeException( 
				"Unexpected key category <" + keyBind.category() + "> from <" + keyBind.name() + ">"
			);
		}
		
		return keyBind;
	}
}
