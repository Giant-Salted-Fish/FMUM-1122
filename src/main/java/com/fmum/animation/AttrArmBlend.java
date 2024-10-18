package com.fmum.animation;

import com.fmum.load.JsonData;
import com.google.gson.JsonElement;
import gsf.util.animation.AnimAttr;
import gsf.util.animation.Track;
import gsf.util.math.MoreMath;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;

public final class AttrArmBlend extends AnimAttr< Float >
{
	public static final AttrArmBlend LEFT_ARM = new AttrArmBlend( "left_arm_blend" );
	public static final AttrArmBlend RIGHT_ARM = new AttrArmBlend( "right_arm_blend" );
	
	
	private final String attr_name;
	
	public AttrArmBlend( String attr_name ) {
		this.attr_name = attr_name;
	}
	
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
	
	@Override
	public Optional< Function< Float, Float > > parse( JsonData data, float anim_len )
	{
		return (
			data.get( this.attr_name )
			.map( JsonElement::getAsJsonObject )
			.filter( obj -> obj.size() > 0 )
			.map( obj -> {
				final float factor = 1.0F / anim_len;
				final Track< Float > track = Track.parse( obj, factor, Float[]::new, JsonElement::getAsFloat );
				return progress -> track.lerp( progress, MoreMath::lerp );
			} )
		);
	}
}
