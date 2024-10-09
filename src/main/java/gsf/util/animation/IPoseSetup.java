package gsf.util.animation;

import gsf.util.math.Quat4f;
import gsf.util.math.Vec3f;
import gsf.util.render.GLUtil;
import gsf.util.render.IPose;

// TODO: Make it work like a matrix stack?
public interface IPoseSetup extends IPose
{
	IPoseSetup EMPTY = new IPoseSetup() {
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
	
	
	static IPoseSetup of( Vec3f pos, Quat4f rot, float factor )
	{
		return new IPoseSetup() {
			@Override
			public void getPos( Vec3f dst ) {
				dst.set( pos );
			}
			
			@Override
			public void getRot( Quat4f dst ) {
				dst.set( rot );
			}
			
			@Override
			public float getFactor() {
				return factor;
			}
			
			@Override
			public void glApply()
			{
				GLUtil.glTranslateV3f( pos );
				GLUtil.glRotateQ4f( rot );
			}
		};
	}
}
