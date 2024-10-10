package gsf.util.animation;

import gsf.util.math.Quat4f;
import gsf.util.math.Vec3f;
import gsf.util.render.IPose;

public interface IAnimCursor extends IPose
{
	IAnimCursor EMPTY = new IAnimCursor() {
		@Override
		public void getPos( Vec3f dst ) {
			dst.setZero();
		}
		
		@Override
		public void getRot( Quat4f dst ) {
			dst.clearRot();
		}
		
		@Override
		public float getFactor() {
			return 1.0F;
		}
		
		@Override
		public void glApply() {
			// Pass.
		}
	};
	
	
	float getFactor();
	
	
	static IAnimCursor of( IPose pose, float factor )
	{
		return new IAnimCursor() {
			@Override
			public void getPos( Vec3f dst ) {
				pose.getPos( dst );
			}
			
			@Override
			public void getRot( Quat4f dst ) {
				pose.getRot( dst );
			}
			
			@Override
			public float getFactor() {
				return factor;
			}
			
			@Override
			public void glApply() {
				pose.glApply();
			}
		};
	}
}
