package gsf.util.animation;

@FunctionalInterface
public interface IAnimation
{
	IAnimation EMPTY = progress -> IAnimator.NONE;
	
	String CHANNEL_NONE = "__none__";
	
	/**
	 * @param progress Range from {@code 0.0F-1.0F}.
	 * @return A cursor to access each animation channel.
	 */
	IAnimator ofProgress( float progress );
}
