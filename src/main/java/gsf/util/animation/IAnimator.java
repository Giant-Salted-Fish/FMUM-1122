package gsf.util.animation;

import gsf.util.render.IPose;

@FunctionalInterface
public interface IAnimator
{
	IAnimator NONE = channel -> IPose.EMPTY;
	
	
	IPose getChannel( String channel );
	
	
	/**
	 * @see IPose#compose(IPose, IPose)
	 */
	static IAnimator compose( IAnimator left, IAnimator right )
	{
		return channel -> {
			final IPose left_pose = left.getChannel( channel );
			final IPose right_pose = right.getChannel( channel );
			return IPose.compose( left_pose, right_pose );
		};
	}
}
