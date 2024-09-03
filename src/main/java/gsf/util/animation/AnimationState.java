package gsf.util.animation;

public abstract class AnimationState
{
	public static final AnimationState EMPTY = new AnimationState() {
		@Override
		public IPoseSetup ofChannel( String channel ) {
			return IPoseSetup.EMPTY;
		}
		
		@Override
		public IAnimation getAnimation() {
			return IAnimation.EMPTY;
		}
	};
	
	public abstract IPoseSetup ofChannel( String channel );
	
	public abstract IAnimation getAnimation();
}
