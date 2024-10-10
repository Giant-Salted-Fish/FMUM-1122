package gsf.util.animation;

import com.fmum.player.PlayerCamera;
import com.google.common.base.MoreObjects;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import gsf.util.math.Mat4f;
import gsf.util.math.Quat4f;
import gsf.util.math.Vec3f;

import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;

public class Animation implements IAnimation
{
	public final HashMap< String, Bone > channels = new HashMap<>();
	
	@Override
	public IAnimator query( float progress )
	{
		return new IAnimator() {
			private final HashMap< String, IAnimCursor > cache = new HashMap<>();
			
			@Override
			public IAnimCursor getChannel( String channel )
			{
				// This is not thread safe because of the possible nested calls.
				final IAnimCursor setup = this.cache.computeIfAbsent( channel, c -> {
					final Bone bone = Animation.this.channels.get( channel );
					return bone == null ? null : bone.getPoseSetup( progress, this );
				} );
				return MoreObjects.firstNonNull( setup, IAnimCursor.EMPTY );
			}
			
			@Override
			public < T > Optional< T > getAttr( AnimAttr< T > attr )
			{
				if ( attr == FloatAttr.LEFT_HAND_BLEND )
				{
					final IAnimCursor left = this.cache.get( "left_arm" );
					return Optional.ofNullable( left ).map( IAnimCursor::getFactor ).map( attr::cast );
				}
				if ( attr == FloatAttr.RIGHT_HAND_BLEND )
				{
					final IAnimCursor right = this.cache.get( "right_arm" );
					return Optional.ofNullable( right ).map( IAnimCursor::getFactor ).map( attr::cast );
				}
				return IAnimator.super.getAttr( attr );
			}
		};
	}
	
	
	public static Animation fromBBJson( JsonObject obj, Gson ctx )
	{
		final Animation anim = new Animation();
		final float anim_len = obj.get( "animation_length" ).getAsFloat();
		final float pos_scale = (
			Optional.ofNullable( obj.get( "position_scale" ) )
			.map( JsonElement::getAsFloat )
			.orElse( 1.0F )
		);
		final JsonObject bones = obj.getAsJsonObject( "bones" );
		
		// Camera channel is a bit special, process it independently.
		final JsonObject cam_obj = bones.getAsJsonObject( PlayerCamera.CHANNEL_CAMERA );
		if ( cam_obj != null )
		{
			final Bone cam_bone = __loadBone(
				cam_obj,
				anim_len,
				e -> {
					final Vec3f pos = ctx.fromJson( e, Vec3f.class );
					pos.x = -pos.x;
					pos.y = -pos.y;
					pos.scale( pos_scale );
					return pos;
				},
				e -> {
					final Vec3f euler = ctx.fromJson( e, Vec3f.class );
					final Mat4f mat = Mat4f.allocate();
					mat.setIdentity();
					mat.rotateZ( euler.z );
					mat.rotateY( euler.y );
					mat.rotateX( -euler.x );
					return Quat4f.rotOf( mat );
				}
			);
			anim.channels.put( PlayerCamera.CHANNEL_CAMERA, cam_bone );
		}
		
		bones.entrySet().forEach( entry -> {
			final String channel = entry.getKey();
			if ( channel.equals( PlayerCamera.CHANNEL_CAMERA ) ) {
				return;
			}
			
			final Bone bone = __loadBone(
				entry.getValue().getAsJsonObject(),
				anim_len,
				e -> {
					final Vec3f pos = ctx.fromJson( e, Vec3f.class );
					pos.x = -pos.x;
					pos.scale( pos_scale );
					return pos;
				},
				e -> {
					final Vec3f euler = ctx.fromJson( e, Vec3f.class );
					final Mat4f mat = Mat4f.allocate();
					mat.setIdentity();
					mat.rotateZ( euler.z );
					mat.rotateY( -euler.y );
					mat.rotateX( -euler.x );
					return Quat4f.rotOf( mat );
				}
			);
			anim.channels.put( channel, bone );
		} );
		return anim;
	}
	
	private static Bone __loadBone(
		JsonObject obj,
		float animation_length,
		Function< JsonElement, Vec3f > pos_parser,
		Function< JsonElement, Quat4f > rot_parser
	) {
		final Bone.Builder builder = new Bone.Builder();
		final JsonPrimitive parent = obj.getAsJsonPrimitive( "parent" );
		if ( parent != null ) {
			builder.setParent( parent.getAsString() );
		}
		
		final float factor = 1.0F / animation_length;
		final JsonObject pos_track = obj.getAsJsonObject( "position" );
		if ( pos_track != null )
		{
			pos_track.entrySet().forEach( e -> {
				final float time = Float.parseFloat( e.getKey() );
				final float progress = time * factor;
				final Vec3f pos = pos_parser.apply( e.getValue() );
				builder.addPos( progress, pos );
			} );
		}
		
		final JsonObject rot_track = obj.getAsJsonObject( "rotation" );
		if ( rot_track != null )
		{
			rot_track.entrySet().forEach( e -> {
				final float time = Float.parseFloat( e.getKey() );
				final float progress = time * factor;
				final Quat4f rot = rot_parser.apply( e.getValue() );
				builder.addRot( progress, rot );
			} );
		}
		
		final JsonObject alpha_track = obj.getAsJsonObject( "alpha" );
		if ( alpha_track != null )
		{
			alpha_track.entrySet().forEach( e -> {
				final float time = Float.parseFloat( e.getKey() );
				final float progress = time * factor;
				final float alpha = e.getValue().getAsFloat();
				builder.addAlpha( progress, alpha );
			} );
		}
		
		return builder.build();
	}
}
