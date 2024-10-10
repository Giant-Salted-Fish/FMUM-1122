package gsf.util.animation;

import gsf.util.render.IPose;

import java.util.Optional;

@FunctionalInterface
public interface IAnimator
{
	IAnimator NONE = channel -> IPose.EMPTY;
	
	
	IPose getChannel( String channel );
	
	default < T > Optional< T > getAttr( AnimAttr< T > attr ) {
		return Optional.empty();
	}
	
	
	/**
	 * @see IPose#compose(IPose, IPose)
	 */
	static IAnimator compose( IAnimator left, IAnimator right )
	{
		return new IAnimator() {
			@Override
			public IPose getChannel( String channel )
			{
				final IPose left_pose = left.getChannel( channel );
				final IPose right_pose = right.getChannel( channel );
				return IPose.compose( left_pose, right_pose );
			}
			
			@Override
			public < T > Optional< T > getAttr( AnimAttr< T > attr )
			{
				final T left_val = left.getAttr( attr ).orElse( null );
				final T right_val = right.getAttr( attr ).orElse( null );
				return attr.compose( left_val, right_val );
			}
		};
	}
}
