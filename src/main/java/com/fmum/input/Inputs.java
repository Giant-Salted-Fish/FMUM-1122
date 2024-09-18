package com.fmum.input;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public final class Inputs
{
	// Category: Common
	public static final String FREE_VIEW = "free_view";
	
	// Category: Gun
	public static final String PULL_TRIGGER = "pull_trigger";
	public static final String TOGGLE_ADS = "toggle_ads";
	public static final String NEXT_FIRE_MODE = "next_fire_mode";
	public static final String RELOAD = "reload_weapon";
	public static final String LOAD_OR_UNLOAD_MAG = "load_or_unload_mag";
	public static final String CHARGE_GUN = "charge_gun";
	public static final String RELEASE_BOLT = "release_bolt";
	public static final String INSPECT_WEAPON = "inspect_weapon";
	public static final String LOAD_AMMO = "load_ammo";
	public static final String UNLOAD_AMMO = "unload_ammo";
	public static final String ALT_AMMO = "alt_ammo";
	
	// Category: Modification
	public static final String OPEN_MODIFY_VIEW = "open_modify_view";
	public static final String NEXT_MODIFY_MODE = "next_modify_mode";
	public static final String ENTER_LAYER = "enter_layer";
	public static final String QUIT_LAYER = "quit_layer";
	public static final String LAST_SLOT = "last_slot";
	public static final String NEXT_SLOT = "next_slot";
	public static final String LAST_MODULE = "last_module";
	public static final String NEXT_MODULE = "next_module";
	public static final String LAST_PREVIEW = "last_preview";
	public static final String NEXT_PREVIEW = "next_preview";
	public static final String LAST_CHANGE = "last_change";
	public static final String NEXT_CHANGE = "next_change";
	public static final String CONFIRM_CHANGE = "confirm_change";
	public static final String REMOVE_MODULE = "remove_module";
	
	private Inputs() { }
}
