package gsf.util.animation;

import com.fmum.animation.FloatAttr;
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
	
	
	public static Animation fromBBJson( JsonObject obj, Gson gson )
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
					final Vec3f pos = gson.fromJson( e, Vec3f.class );
					pos.x = -pos.x;
					pos.y = -pos.y;
					pos.scale( pos_scale );
					return pos;
				},
				e -> {
					final Vec3f euler = gson.fromJson( e, Vec3f.class );
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
					final Vec3f pos = gson.fromJson( e, Vec3f.class );
					pos.x = -pos.x;
					pos.scale( pos_scale );
					return pos;
				},
				e -> {
					final Vec3f euler = gson.fromJson( e, Vec3f.class );
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
		final JsonPrimitive p_node = obj.getAsJsonPrimitive( "parent" );
		final String parent = p_node != null ? p_node.getAsString() : IAnimation.CHANNEL_NONE;
		
		final float factor = 1.0F / animation_length;
		final JsonObject pos_node = obj.getAsJsonObject( "position" );
		final Track< Vec3f > pos_track = (
			pos_node != null && pos_node.size() > 0
			? Track.from( pos_node, factor, Vec3f[]::new, pos_parser )
			: Track.EMPTY_POS_TRACK
		);
		
		final JsonObject rot_node = obj.getAsJsonObject( "rotation" );
		final Track< Quat4f > rot_track = (
			rot_node != null && rot_node.size() > 0
			? Track.from( rot_node, factor, Quat4f[]::new, rot_parser )
			: Track.EMPTY_ROT_TRACK
		);
		
		final JsonObject alpha_node = obj.getAsJsonObject( "alpha" );
		final Track< Float > alpha_track = (
			alpha_node != null && alpha_node.size() > 0
			? Track.from( alpha_node, factor, Float[]::new, JsonElement::getAsFloat )
			: Track.EMPTY_ALPHA_TRACK
		);
		
		return new Bone( parent, pos_track, rot_track, alpha_track );
	}
}
