package com.fmum.client.input;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public final class InputCmd
{
	public static final String
		FREE_VIEW = "free_view";

	public static final String
		PULL_TRIGGER = "pull_trigger",
		TOGGLE_ADS = "toggle_ads",

		SWITCH_FIRE_MODE = "switch_fire_mode",
		RELOAD_WEAPON = "reload_weapon",
		LOAD_OR_UNLOAD_MAG = "load_or_unload_mag",

		CHARGE_GUN = "charge_gun",
		RELEASE_BOLT = "release_bolt",

		INSPECT_WEAPON = "inspect_weapon";

	public static final String
		OPEN_MODIFY_VIEW = "open_modify_view",
		SWITCH_MODIFY_MODE = "switch_modify_mode",

		ENTER_LAYER = "enter_layer",
		QUIT_LAYER = "quit_layer",

		SWITCH_TO_NEXT_SLOT = "switch_to_next_slot",
		SWITCH_TO_LAST_SLOT = "switch_to_last_slot",
		SWITCH_TO_NEXT_MODULE = "switch_to_next_module",
		SWITCH_TO_LAST_MODULE = "switch_to_last_module",

		CONFIRM_CHANGES = "confirm_changes";


	public InputCmd( String ) { }
}
