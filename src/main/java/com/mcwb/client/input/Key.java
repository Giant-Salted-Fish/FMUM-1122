package com.mcwb.client.input;

import com.mcwb.common.MCWB;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * You can find the names of all {@link MCWB} pre-defined keys in this class
 * 
 * @see InputHandler
 * @author Giant_Salted_Fish
 */
@SideOnly( Side.CLIENT )
public final class Key
{
	/**
	 * For keys that are general
	 */
	public static final String CATEGORY_GENERAL = "mcwb.key_group.general";
	
	public static final String
		FREE_VIEW = "free_view";
	
	/**
	 * For keys that operate a gun
	 */
	public static final String CATEGORY_GUN = "mcwb.key_group.gun";
	
	public static final String
		PULL_TRIGGER = "pull_trigger",
		AIM_HOLD     = "aim_hold",
		AIM_TOGGLE   = "aim_toggle";
	
	/**
	 * For keys that provides assist
	 */
	public static final String CATEGORY_ASSIST = "mcwb.key_group.assist";
	
	public static final String
		CO = "co",
		CO_FREE_VIEW     = "co_free_view",
		CO_TOGGLE_MODIFY = "co_toggle_modify";
	
	/**
	 * For keys that being used to modify a weapon
	 */
	public static final String CATEGORY_MODIFY = "mcwb.key_group.modify";
	
	public static final String
		TOGGLE_MODIFY  = "toggle_modify",
		SELECT_TOGGLE  = "select_toggle",
		SELECT_UP      = "select_up",
		SELECT_DOWN    = "select_down",
		SELECT_LEFT    = "select_left",
		SELECT_RIGHT   = "select_right",
		SELECT_CONFIRM = "select_confirm",
		SELECT_CANCEL  = "select_cancel";
	
	/**
	 * Other keys
	 */
	public static final String CATEGORY_OTHER = "mcwb.key_group.other";
	
	private Key() { }
}
