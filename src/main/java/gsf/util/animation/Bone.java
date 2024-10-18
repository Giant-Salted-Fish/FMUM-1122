package gsf.util.animation;

import gsf.util.math.MoreMath;
import gsf.util.math.Quat4f;
import gsf.util.math.Vec3f;
import gsf.util.render.IPose;

import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.IntFunction;

public class Bone
{
	private final String parent;
	
	private final Track< Vec3f > pos_track;
	private final Track< Quat4f > rot_track;
	private final Track< Float > alpha_track;
	
	public Bone(
		String parent,
		Track< Vec3f > pos_track,
		Track< Quat4f > rot_track,
		Track< Float > alpha_track
	) {
		this.parent = parent;
		this.pos_track = pos_track;
		this.rot_track = rot_track;
		this.alpha_track = alpha_track;
	}
	
	public IAnimCursor getPoseSetup( float progress, IAnimator cursor )
	{
		// Alpha track.
		final Float factor = this.alpha_track.lerp( progress, MoreMath::lerp );
		
		// Position track.
		final Vec3f pos = this.pos_track.lerp( progress, ( left, right, alpha ) -> {
			final Vec3f dst = new Vec3f();
			dst.interpolate( left, right, alpha );
			return dst;
		} );
		
		// Rotation track.
		final Quat4f rot = this.rot_track.lerp( progress, ( left, right, alpha ) -> {
			final Quat4f dst = new Quat4f();
			dst.interpolate( left, right, alpha );
			return dst;
		} );
		
		// Get parent track.
		if ( this.parent.equals( IAnimation.CHANNEL_NONE ) ) {
			return IAnimCursor.of( IPose.of( pos, rot ), factor );
		}
		else
		{
			final IPose parent = cursor.getChannel( this.parent );
			final IPose pose = IPose.compose( parent, IPose.of( pos, rot ) );
			return IAnimCursor.of( pose, factor );
		}
	}
}
