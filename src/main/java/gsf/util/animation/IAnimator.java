package gsf.util.animation;

@FunctionalInterface
public interface IAnimator
{
	IAnimator NONE = channel -> IPoseSetup.EMPTY;
	
	
	IPoseSetup getChannel( String channel );
	
	
	static IAnimator compose( IAnimator left, IAnimator right )
	{
		return channel -> {
			final IPoseSetup left_pose = left.getChannel( channel );
			final IPoseSetup right_pose = right.getChannel( channel );
			return IPoseSetup.compose( left_pose, right_pose );
		};
	}
}
