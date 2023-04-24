package com.mcwb.client.input;

import static com.mcwb.client.input.InputHandler.*;

import org.lwjgl.input.Keyboard;

import com.mcwb.common.MCWB;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * You can find the names of all {@link MCWB} pre-defined keys in this class.
 * 
 * @see InputHandler
 * @author Giant_Salted_Fish
 */
@SideOnly( Side.CLIENT )
public final class Key
{
	public static final class Category
	{
		public static final String
			GENERAL = "mcwb.key_category.general",
			GUN     = "mcwb.key_category.gun",
			ASSIST  = "mcwb.key_category.assist",
			MODIFY  = "mcwb.key_category.modify",
			OTHER   = "mcwb.key_category.other";
		
		private Category() { }
	}
	
	private static final int MOUSE_0 = -100, MOUSE_1 = -99;
	
	/**
	 * These keys always update.
	 */
	public static final KeyBind
		PULL_TRIGGER = new KeyBind( "pull_trigger", Category.GUN, MOUSE_0, GLOBAL_KEYS ),
		AIM_HOLD = new KeyBind( "aim_hold", Category.GUN, MOUSE_1, GLOBAL_KEYS ),
		AIM_TOGGLE = new KeyBind( "aim_toggle", Category.GUN, Keyboard.KEY_NONE, GLOBAL_KEYS ),
//		LOAD_AMMO = new KeyBind( "load_ammo", Category.GUN, MOUSE_0 ),
//		UNLOAD_AMMO = new KeyBind( "unload_ammo", Category.GUN, MOUSE_1 ),
		
		// TODO: change the default bind key for this maybe
		SELECT_TOGGLE = new KeyBind( "select_toggle", Category.MODIFY, Keyboard.KEY_V ),
		SELECT_UP = new KeyBind( "select_up", Category.MODIFY, Keyboard.KEY_UP ),
		SELECT_DOWN = new KeyBind( "select_down", Category.MODIFY, Keyboard.KEY_DOWN ),
		SELECT_LEFT = new KeyBind( "select_left", Category.MODIFY, Keyboard.KEY_LEFT ),
		SELECT_RIGHT = new KeyBind( "select_right", Category.MODIFY, Keyboard.KEY_RIGHT ),
		SELECT_CONFIRM = new KeyBind( "select_confirm", Category.MODIFY, Keyboard.KEY_G ),
		SELECT_CANCEL = new KeyBind( "select_cancel", Category.MODIFY, Keyboard.KEY_H ),
		
		ASSIST = new KeyBind( "assist", Category.ASSIST, Keyboard.KEY_Z, GLOBAL_KEYS );
	
	/**
	 * These keys will update if {@link #ASSIST} is not down.
	 */
	public static final KeyBind
		FREE_VIEW = new KeyBind( "free_view", Category.GENERAL, Keyboard.KEY_LMENU ),
		TOGGLE_MODIFY = new KeyBind( "toggle_modify", Category.GUN, Keyboard.KEY_I ),
		
		RELOAD = new KeyBind( "reload", Category.GUN, Keyboard.KEY_R ),
		LOAD_UNLOAD_MAG = new KeyBind( "load_unload_mag", Category.GUN, Keyboard.KEY_T ),
		CHARGE_GUN = new KeyBind( "charge_gun", Category.GUN, Keyboard.KEY_G ),
		INSPECT = new KeyBind( "inspect", Category.GUN, Keyboard.KEY_V );
	
	/**
	 * These keys will update if {@link #ASSIST} is down.
	 */
	public static final KeyBind
		CO_FREE_VIEW = new KeyBind( "co_free_view", Category.ASSIST, Keyboard.KEY_NONE ),
		CO_RELOAD = new KeyBind( "co_reload", Category.ASSIST, Keyboard.KEY_NONE ),
		CO_LOAD_UNLOAD_MAG = new KeyBind( "co_load_unload_mag", Category.ASSIST, Keyboard.KEY_NONE ),
		CO_CHARGE_GUN = new KeyBind( "co_charge_gun", Category.ASSIST, Keyboard.KEY_NONE ),
		CO_INSPECT = new KeyBind( "co_inspect", Category.ASSIST, Keyboard.KEY_NONE ),
		CO_TOGGLE_MODIFY = new KeyBind( "co_toggle_modify", Category.ASSIST, Keyboard.KEY_NONE );
	
	private Key() { }
}
