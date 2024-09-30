package com.fmum.input;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Optional;

@SideOnly( Side.CLIENT )
public final class InputManager
{
	private static final HashMap< String, BoolInput > BOOL_INPUT_TABLE = new HashMap<>();
	
	
	public static Optional< IInput > getInput( String name )
	{
		final BoolInput input = BOOL_INPUT_TABLE.get( name );
		return Optional.ofNullable( input );
	}
	
	public static boolean getBoolState( String name )
	{
		final BoolInput input = BOOL_INPUT_TABLE.get( name );
		return input != null && input.getAsBool();
	}
	
	// TODO: Maybe need some kind of registry for input signals?
	public static void emitBoolSignal( String name, boolean flag )
	{
		final BoolInput input = BOOL_INPUT_TABLE.computeIfAbsent( name, key -> new BoolInput() );
		final int prev_count = input.signal_count;
		input.signal_count += flag ? 1 : -1;
		if ( input.signal_count + prev_count == 1 )
		{
			final InputUpdateEvent evt = new InputUpdateEvent( name, input );
			MinecraftForge.EVENT_BUS.post( evt );
		}
	}
	
	
	private static final class BoolInput implements IInput
	{
		private int signal_count = 0;
		
		@Override
		public boolean getAsBool() {
			return this.signal_count > 0;
		}
	}
	
	private InputManager() { }
}
