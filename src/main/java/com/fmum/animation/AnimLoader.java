package com.fmum.animation;

import com.fmum.load.JsonData;
import com.fmum.player.IPlayerCamera;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.util.Pair;
import gsf.util.animation.AnimAttr;
import gsf.util.animation.Bone;
import gsf.util.animation.IAnimation;
import gsf.util.animation.Track;
import gsf.util.math.Mat4f;
import gsf.util.math.Quat4f;
import gsf.util.math.Vec3f;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class AnimLoader
{
	public static IAnimation fromBBJson(
		JsonObject obj,
		JsonDeserializationContext ctx,
		AnimAttr< ? >... attrs
	) {
		final JsonData data = new JsonData( obj, ctx );
		final float anim_len = data.getFloat( "animation_length" ).orElse( 1.0F );
		final float pos_scale = data.getFloat( "position_scale" ).orElse( 1.0F );
		final Map< String, Bone > channels = (
			data.getData( "bones" )
			.map( bones -> (
				bones.obj.entrySet().stream()
				.map( entry -> {
					final String channel = entry.getKey();
					final JsonElement value = entry.getValue();
					final Function< JsonElement, Vec3f > pos_parser;
					final Function< JsonElement, Quat4f > rot_parser;
					if ( channel.equals( IPlayerCamera.CHANNEL_CAMERA ) )
					{
						// Camera channel is a bit special, process it independently.
						pos_parser = e -> {
							final Vec3f pos = ctx.deserialize( e, Vec3f.class );
							pos.x = -pos.x;
							pos.y = -pos.y;
							pos.scale( pos_scale );
							return pos;
						};
						rot_parser = e -> {
							final Vec3f euler = ctx.deserialize( e, Vec3f.class );
							final Mat4f mat = Mat4f.allocate();
							mat.setIdentity();
							mat.rotateZ( euler.z );
							mat.rotateY( euler.y );
							mat.rotateX( -euler.x );
							return Quat4f.rotOf( mat );
						};
					}
					else  // All rest of the bones.
					{
						pos_parser = e -> {
							final Vec3f pos = ctx.deserialize( e, Vec3f.class );
							pos.x = -pos.x;
							pos.scale( pos_scale );
							return pos;
						};
						rot_parser = e -> {
							final Vec3f euler = ctx.deserialize( e, Vec3f.class );
							final Mat4f mat = Mat4f.allocate();
							mat.setIdentity();
							mat.rotateZ( euler.z );
							mat.rotateY( -euler.y );
							mat.rotateX( -euler.x );
							return Quat4f.rotOf( mat );
						};
					}
					final JsonData dat = new JsonData( value.getAsJsonObject(), ctx );
					final Bone bone = __loadBone( dat, anim_len, pos_parser, rot_parser );
					return Pair.of( channel, bone );
				} )
				.collect( Collectors.toMap( Pair::first, Pair::second ) )
			) )
			.orElse( Collections.emptyMap() )
		);
		
		final Map< AnimAttr< ? >, Function< Float, ? > > attr_map = (
			Arrays.stream( attrs )
			.map( attr -> attr.parse( data, anim_len ).map( val -> Pair.of( attr, val ) ) )
			.filter( Optional::isPresent )
			.map( Optional::get )
			.collect( Collectors.toMap( Pair::first, Pair::second ) )
		);
		
		return IAnimation.of( channels, attr_map );
	}
	
	private static Bone __loadBone(
		JsonData data,
		float animation_length,
		Function< JsonElement, Vec3f > pos_parser,
		Function< JsonElement, Quat4f > rot_parser
	) {
		final String parent = data.getString( "parent" ).orElse( IAnimation.CHANNEL_NONE );
		
		final float factor = 1.0F / animation_length;
		final Track< Vec3f > pos_track = (
			data.get( "position" )
			.map( JsonElement::getAsJsonObject )
			.filter( node -> node.size() > 0 )
			.map( node -> Track.parse( node, factor, Vec3f[]::new, pos_parser ) )
			.orElse( Track.EMPTY_POS_TRACK )
		);
		
		final Track< Quat4f > rot_track = (
			data.get( "rotation" )
			.map( JsonElement::getAsJsonObject )
			.filter( node -> node.size() > 0 )
			.map( node -> Track.parse( node, factor, Quat4f[]::new, rot_parser ) )
			.orElse( Track.EMPTY_ROT_TRACK )
		);
		
		return new Bone( parent, pos_track, rot_track );
	}
	
	
	private AnimLoader() { }
}
