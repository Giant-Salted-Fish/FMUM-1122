package com.fmum.client.input;

import com.fmum.client.player.PlayerPatchClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Optional;

@SideOnly( Side.CLIENT )
public final class InputManager
{
	
	private static final HashMap< String, BoolInput >
		SIGNAL_2_BOOL_INPUT = new HashMap<>();
	
	private InputManager() { }
	
	public static IInput getInput( String signal )
	{
		// TODO: Maybe not nullable?
		return Optional.< IInput >ofNullable(
			SIGNAL_2_BOOL_INPUT.get( signal ) ).orElse( () -> false );
	}
	
	public static void emitBoolSignal( String signal, boolean flag )
	{
		final BoolInput input = SIGNAL_2_BOOL_INPUT
			.computeIfAbsent( signal, key -> new BoolInput() );
		final int prev_count = input.active_count;
		input.active_count += flag ? 1 : -1;
		if ( input.active_count + prev_count == 1 ) {
			PlayerPatchClient.instance.onInputSignal( signal, input );
		}
	}
	
	private static final class BoolInput implements IInput
	{
		private int active_count = 0;
		
		@Override
		public boolean asBool() {
			return this.active_count > 0;
		}
	}
}
