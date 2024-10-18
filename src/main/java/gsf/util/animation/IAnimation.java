package gsf.util.animation;

import com.google.common.base.MoreObjects;
import gsf.util.render.IPose;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@FunctionalInterface
public interface IAnimation
{
	IAnimation EMPTY = progress -> IAnimator.NONE;
	
	String CHANNEL_NONE = "__none__";
	
	/**
	 * @param progress Range from {@code 0.0F-1.0F}.
	 * @return A cursor to access each animation channel.
	 */
	IAnimator query( float progress );
	
	
	static IAnimation of(
		Map< String, Bone > channels,
		Map< AnimAttr< ? >, Function< Float, ? > > attrs
	) {
		return progress -> new IAnimator() {
			private final HashMap< String, IPose > channel_cache = new HashMap<>();
			
			@Override
			public IPose getChannel( String channel )
			{
				// This is not thread safe because of the possible nested calls.
				final IPose setup = this.channel_cache.computeIfAbsent( channel, c -> {
					final Bone bone = channels.get( channel );
					return bone == null ? null : bone.getPoseSetup( progress, this );
				} );
				return MoreObjects.firstNonNull( setup, IPose.EMPTY );
			}
			
			@Override
			public < T > Optional< T > getAttr( AnimAttr< T > attr )
			{
				return (
					Optional.ofNullable( attrs.get( attr ) )
					.map( f -> f.apply( progress ) )
					.map( attr::cast )
				);
			}
		};
	}
}
