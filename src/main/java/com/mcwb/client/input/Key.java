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
	
	/**
	 * For keys that are general
	 */
	public static final String
		FREE_VIEW = "free_view";
	
	/**
	 * For keys that operate a gun
	 */
	public static final String
		PULL_TRIGGER    = "pull_trigger",
		AIM_HOLD        = "aim_hold",
		AIM_TOGGLE      = "aim_toggle",
		RELOAD          = "reload",
		LOAD_UNLOAD_MAG = "load_unload_mag",
		INSPECT         = "inspect";
	
	/**
	 * For keys that provides assist
	 */
	public static final String
		CO                 = "co",
		CO_FREE_VIEW       = "co_free_view",
		CO_RELOAD          = "co_reload",
		CO_LOAD_UNLOAD_MAG = "co_load_unload_mag",
		CO_INSPECT         = "co_inspect",
		CO_TOGGLE_MODIFY   = "co_toggle_modify";
	
	/**
	 * For keys that being used to modify a weapon
	 */
	public static final String
		TOGGLE_MODIFY  = "toggle_modify",
		SELECT_TOGGLE  = "select_toggle",
		SELECT_UP      = "select_up",
		SELECT_DOWN    = "select_down",
		SELECT_LEFT    = "select_left",
		SELECT_RIGHT   = "select_right",
		SELECT_CONFIRM = "select_confirm",
		SELECT_CANCEL  = "select_cancel";
	
	private Key() { }
}
