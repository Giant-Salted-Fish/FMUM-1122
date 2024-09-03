package gsf.util.animation;

import com.google.common.base.MoreObjects;

import java.util.HashMap;

public class Animation implements IAnimation
{
	public final HashMap< String, Bone > channels = new HashMap<>();
	
	@Override
	public AnimationState ofProgress( float progress )
	{
		final HashMap< String, IPoseSetup > cache = new HashMap<>();
		return new AnimationState() {
			@Override
			public IPoseSetup ofChannel( String channel )
			{
				// This is not thread safe because of the possible nested calls.
				final IPoseSetup setup = cache.computeIfAbsent( channel, c -> {
					final Bone bone = Animation.this.channels.get( channel );
					return bone != null ? bone.getPoseSetup( progress, this ) : null;
				} );
				return MoreObjects.firstNonNull( setup, IPoseSetup.EMPTY );
			}
			
			@Override
			public IAnimation getAnimation() {
				return Animation.this;
			}
		};
	}
}
