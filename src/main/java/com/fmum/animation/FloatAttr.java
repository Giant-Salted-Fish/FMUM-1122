package com.fmum.animation;

import gsf.util.animation.AnimAttr;

import javax.annotation.Nullable;
import java.util.Optional;

public class FloatAttr extends AnimAttr< Float >
{
	public static final FloatAttr LEFT_HAND_BLEND = new FloatAttr();
	public static final FloatAttr RIGHT_HAND_BLEND = new FloatAttr();
	
	
	@Override
	public Optional< Float > compose( @Nullable Float left, @Nullable Float right )
	{
		final int case_id = ( left == null ? 0 : 1 ) | ( right == null ? 0 : 2 );
		switch( case_id )
		{
		case 0b00:
			return Optional.empty();
		case 0b01:
			return Optional.of( left );
		case 0b10:
			return Optional.of( right );
		case 0b11:
			return Optional.of( 0.5F * ( left + right ) );
		default:
			throw new IllegalStateException( "Invalid case_id: " + case_id );
		}
	}
}
