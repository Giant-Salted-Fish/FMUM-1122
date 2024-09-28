package gsf.util.animation;

import gsf.util.math.MoreMath;
import gsf.util.math.Quat4f;
import gsf.util.math.Vec3f;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.IntFunction;

public class Bone
{
	private final String parent;
	
	private final Track< Vec3f > pos_track;
	private final Track< Quat4f > rot_track;
	private final Track< Float > alpha_track;
	
	private Bone(
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
	
	public IPoseSetup getPoseSetup( float progress, IAnimator cursor )
	{
		// Get parent track.
		final IPoseSetup parent = cursor.getChannel( this.parent );
		
		// Alpha track.
		final float alpha;
		{
			final Track< Float > track = this.alpha_track;
			final float[] keys = track.keys;
			final Float[] values = track.values;
			final int key = Arrays.binarySearch( track.keys, progress );
			final int idx = key < 0 ? -key - 1 : key;
			final int floor = Math.max( 0, idx - 1 );
			final int ceiling = Math.min( keys.length - 1, idx );
			final float a = __shiftDiv( progress, keys[ ceiling ], -keys[ floor ] );
			alpha = MoreMath.lerp( values[ floor ], values[ ceiling ], a );
		}
		
		// Position track.
		final Vec3f pos = new Vec3f();
		final Quat4f rot = new Quat4f();
		{
			final Track< Vec3f > track = this.pos_track;
			final float[] keys = track.keys;
			final Vec3f[] values = track.values;
			final int key = Arrays.binarySearch( track.keys, progress );
			final int idx = key < 0 ? -key - 1 : key;
			final int floor = Math.max( 0, idx - 1 );
			final int ceiling = Math.min( keys.length - 1, idx );
			final float a = __shiftDiv( progress, keys[ ceiling ], -keys[ floor ] );
			pos.interpolate( values[ floor ], values[ ceiling ], a );
			
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
			final Track< Quat4f > track = this.rot_track;
			final float[] keys = track.keys;
			final Quat4f[] values = track.values;
			final int key = Arrays.binarySearch( track.keys, progress );
			final int idx = key < 0 ? -key - 1 : key;
			final int floor = Math.max( 0, idx - 1 );
			final int ceiling = Math.min( keys.length - 1, idx );
			final float a = __shiftDiv( progress, keys[ ceiling ], -keys[ floor ] );
			
			final Quat4f quat = Quat4f.allocate();
			quat.interpolate( values[ floor ], values[ ceiling ], a );
			rot.mul( rot, quat );
			Quat4f.release( quat );
		}
		
		return IPoseSetup.of( pos, rot, alpha );
	}
	
	/**
	 * @return {@code (a + shift) / (b + shift)}, {@code 0.0F} if {@code b + shift == 0.0F}.
	 */
	private static float __shiftDiv( float a, float b, float shift )
	{
		float divisor = b + shift;
		return divisor != 0.0F ? ( a + shift ) / divisor : 0.0F;
	}
	
	
	private static final class Track< T >
	{
		private final float[] keys;
		private final T[] values;
		
		private Track( float[] keys, T[] values )
		{
			this.keys = keys;
			this.values = values;
		}
	}
	
	
	public static class Builder
	{
		private static final Track< Float > FALLBACK_ALPHA_TRACK = new Track<>( new float[] { 0.0F }, new Float[] { 0.0F } );
		private static final Track< Vec3f > FALLBACK_POS_TRACK = new Track<>( new float[] { 0.0F }, new Vec3f[] { Vec3f.ORIGIN } );
		private static final Track< Quat4f > FALLBACK_ROT_TRACK = new Track<>( new float[] { 0.0F }, new Quat4f[] { Quat4f.IDENTITY } );
		
		private String parent = IAnimation.CHANNEL_NONE;
		private final TreeMap< Float, Vec3f > pos = new TreeMap<>();
		private final TreeMap< Float, Quat4f > rot = new TreeMap<>();
		private TreeMap< Float, Float > alpha = null;
		
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
				this.pos.isEmpty() ? FALLBACK_POS_TRACK : __buildTrack( this.pos, Vec3f[]::new ),
				this.rot.isEmpty() ? FALLBACK_ROT_TRACK : __buildTrack( this.rot, Quat4f[]::new ),
				this.alpha == null ? FALLBACK_ALPHA_TRACK : __buildTrack( this.alpha, Float[]::new )
			);
		}
		
		private static < T > Track< T > __buildTrack( TreeMap< Float, T > map, IntFunction< T[] > supplier )
		{
			final int size = map.size();
			final float[] keys = new float[ size ];
			final T[] values = supplier.apply( size );
			int i = 0;
			for ( Entry< Float, T > e : map.entrySet() )
			{
				keys[ i ] = e.getKey();
				values[ i ] = e.getValue();
				i += 1;
			}
			return new Track<>( keys, values );
		}
	}
}
