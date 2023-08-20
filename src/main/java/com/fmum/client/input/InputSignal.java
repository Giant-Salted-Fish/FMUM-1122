package com.fmum.client.input;

import com.fmum.client.player.PlayerPatchClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Optional;

@SideOnly( Side.CLIENT )
public final class InputSignal
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
	
	private static final HashMap< String, BoolInput >
		SIGNAL_2_BOOL_INPUT = new HashMap<>();
	
	private InputSignal() { }
	
	public static Input get( String signal )
	{
		// TODO: Maybe not nullable?
		return Optional.< Input >ofNullable(
			SIGNAL_2_BOOL_INPUT.get( signal ) ).orElse( () -> false );
	}
	
	public static void emitBoolSignal( String signal, boolean flag )
	{
		final BoolInput input = SIGNAL_2_BOOL_INPUT
			.computeIfAbsent( signal, key -> new BoolInput() );
		final int prev_count = input.active_count;
		input.active_count += flag ? 1 : -1;
		if ( input.active_count + prev_count == 1 ) {
			PlayerPatchClient.get().onInputSignal( signal, input );
		}
	}
	
	private static final class BoolInput implements Input
	{
		private int active_count = 0;
		
		@Override
		public boolean asBool() {
			return this.active_count > 0;
		}
	}
}
