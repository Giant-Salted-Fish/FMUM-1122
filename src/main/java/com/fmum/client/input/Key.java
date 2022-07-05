package com.fmum.client.input;

import com.fmum.common.FMUM;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A list of all names of the keys that provided by {@link FMUM} in default. You can switch the name
 * of the key to response upon input.
 * 
 * @see InputHandler
 * @author Giant_Salted_Fish
 */
@SideOnly( Side.CLIENT )
public abstract class Key
{
	/**
	 * Name of the keys for test. They will be removed in release build.
	 */
	public static final String
		TEST_UP = "testup",
		TEST_DOWN = "testdown",
		TEST_LEFT = "testleft",
		TEST_RIGHT = "testright",
		TEST_ENTER = "testenter",
		TEST_QUIT = "testquit",
		DEBUG = "debug";
	
	/**
	 * Name of the keys that {@link FMUM} provides by default
	 */
	public static final String
		FIRE = "fire",
		AIM_HOLD = "aimhold",
		AIM_TOGGLE = "aimtoggle",
		TOGGLE_MANUAL = "togglemanual",
		LOOK_AROUND = "lookaround",
		VIEW_WEAPON = "viewweapon",
		TOGGLE_MODIFY = "togglemodify",
		
		CO = "co",
		CO_TOGGLE_MANUAL = "cotogglemanual",
		CO_LOOK_AROUND = "colookaround",
		CO_VIEW_WEAPON = "coviewweapon",
		CO_TOGGLE_MODIFY = "cotogglemodify",
		
		SELECT_UP = "selectup",
		SELECT_DOWN = "selectdown",
		SELECT_LEFT = "selectleft",
		SELECT_RIGHT = "selectright",
		SELECT_CONFIRM = "selectconfirm",
		SELECT_CANCEL = "selectcancel",
		SELECT_TOGGLE = "selecttoggle";
	
	private Key() { }
}
