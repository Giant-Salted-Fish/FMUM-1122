package gsf.util.animation;

import gsf.util.math.MoreMath;
import gsf.util.math.Quat4f;
import gsf.util.math.Vec3f;

import java.util.Map.Entry;
import java.util.TreeMap;

public class Bone
{
	private final TreeMap< Float, Vec3f > pos;
	private final TreeMap< Float, Quat4f > rot;
	private final TreeMap< Float, Float > alpha;
	
	private final String parent;
	
	private Bone( String parent, TreeMap< Float, Vec3f > pos, TreeMap< Float, Quat4f > rot, TreeMap< Float, Float > alpha )
	{
		this.parent = parent;
		this.pos = pos;
		this.rot = rot;
		this.alpha = alpha;
	}
	
	public IPoseSetup getPoseSetup( float progress, IAnimator cursor )
	{
		// Get parent track.
		final IPoseSetup parent = cursor.getChannel( this.parent );
		
		// Alpha track.
		float _alpha;
		{
			final Entry< Float, Float > floor = __floor( this.alpha, progress );
			final Entry< Float, Float > ceil = __ceiling( this.alpha, progress );
			final float a = MoreMath.shiftDiv( progress, ceil.getKey(), -floor.getKey() );
			_alpha = MoreMath.lerp( floor.getValue(), ceil.getValue(), a );
		}
		final float alpha = _alpha;
		
		// Position track.
		final Vec3f pos = new Vec3f();
		final Quat4f rot = new Quat4f();
		{
			final Entry< Float, Vec3f > floor = __floor( this.pos, progress );
			final Entry< Float, Vec3f > ceil = __ceiling( this.pos, progress );
			final float a = MoreMath.shiftDiv( progress, ceil.getKey(), -floor.getKey() );
			pos.interpolate( floor.getValue(), ceil.getValue(), a );
			
			// Apply rotation from parent bone.
			parent.getRot( rot );
			rot.transform( pos, pos );
			
			// Apply translation from parent bone.
			final Vec3f vec = Vec3f.allocate();
			parent.getPos( vec );
			pos.add( vec );
			Vec3f.release( vec );
		}
		
		// Rotation track.
		{
			final Entry< Float, Quat4f > floor = __floor( this.rot, progress );
			final Entry< Float, Quat4f > ceil = __ceiling( this.rot, progress );
			final float a = MoreMath.shiftDiv( progress, ceil.getKey(), -floor.getKey() );
			final Quat4f quat = Quat4f.allocate();
			quat.interpolate( floor.getValue(), ceil.getValue(), a );
			rot.mul( rot, quat );
			Quat4f.release( quat );
		}
		
		return IPoseSetup.of( pos, rot, alpha );
	}
	
	private static < T > Entry< Float, T > __floor( TreeMap< Float, T > src, float progress )
	{
		final Entry< Float, T > entry = src.floorEntry( progress );
		return entry != null ? entry : src.ceilingEntry( progress );
	}
	
	private static < T > Entry< Float, T > __ceiling( TreeMap< Float, T > src, float progress )
	{
		final Entry< Float, T > entry = src.ceilingEntry( progress );
		return entry != null ? entry : src.floorEntry( progress );
	}
	
	
	public static class Builder
	{
		private static final TreeMap< Float, Float > FALLBACK_ALPHA_TRACK = new TreeMap<>();
		private static final TreeMap< Float, Vec3f > FALLBACK_POS_TRACK = new TreeMap<>();
		private static final TreeMap< Float, Quat4f > FALLBACK_ROT_TRACK = new TreeMap<>();
		static
		{
			FALLBACK_ALPHA_TRACK.put( 0.0F, 0.0F );
			FALLBACK_POS_TRACK.put( 0.0F, Vec3f.ORIGIN );
			FALLBACK_ROT_TRACK.put( 0.0F, Quat4f.IDENTITY );
		}
		
		private final TreeMap< Float, Vec3f > pos = new TreeMap<>();
		private final TreeMap< Float, Quat4f > rot = new TreeMap<>();
		private TreeMap< Float, Float > alpha = null;
		private String parent = IAnimation.CHANNEL_NONE;
		
		public Builder setParent( String parent )
		{
			this.parent = parent;
			return this;
		}
		
		public Builder addPos( float time, Vec3f pos )
		{
			this.pos.put( time, pos );
			return this;
		}
		
		public Builder addRot( float time, Quat4f rot )
		{
			this.rot.put( time, rot );
			return this;
		}
		
		public Builder addAlpha( float time, float alpha )
		{
			if ( this.alpha == null ) {
				this.alpha = new TreeMap<>();
			}
			this.alpha.put( time, alpha );
			return this;
		}
		
		public Bone build()
		{
			return new Bone(
				this.parent,
				this.pos.isEmpty() ? FALLBACK_POS_TRACK : this.pos,
				this.rot.isEmpty() ? FALLBACK_ROT_TRACK : this.rot,
				this.alpha != null ? this.alpha : FALLBACK_ALPHA_TRACK
			);
		}
	}
}
