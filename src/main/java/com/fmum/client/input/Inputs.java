package com.fmum.client.input;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public final class Inputs
{
	public static final String
		FREE_VIEW = "free_view";
	
	public static final String
		PULL_TRIGGER = "pull_trigger",
		TOGGLE_ADS = "toggle_ads",
	
		LAST_FIRE_MODE = "last_fire_mode",
		NEXT_FIRE_MODE = "next_fire_mode",
		RELOAD_WEAPON = "reload_weapon",
		LOAD_OR_UNLOAD_MAG = "load_or_unload_mag",
	
		CHARGE_GUN = "charge_gun",
		RELEASE_BOLT = "release_bolt",
	
		INSPECT_WEAPON = "inspect_weapon";
	
	public static final String
		OPEN_MODIFY_VIEW = "open_modify_view",
		LAST_MODIFY_MODE = "last_modify_mode",
		NEXT_MODIFY_MODE = "next_modify_mode",
	
		ENTER_LAYER = "enter_layer",
		QUIT_LAYER = "quit_layer",
	
		LAST_SLOT = "last_slot",
		NEXT_SLOT = "next_slot",
		LAST_MODULE = "last_module",
		NEXT_MODULE = "next_module",
	
		LAST_PREVIEW = "last_preview",
		NEXT_PREVIEW = "next_preview",
		LAST_CHANGE = "last_change",
		NEXT_CHANGE = "next_change",
	
		CONFIRM_CHANGE = "confirm_change",
		REMOVE_MODULE = "remove_module";
	
	private Inputs() { }
}
