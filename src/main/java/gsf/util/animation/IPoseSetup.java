package gsf.util.animation;

import gsf.util.math.Mat4f;
import gsf.util.math.Quat4f;
import gsf.util.math.Vec3f;
import gsf.util.render.GLUtil;

// TODO: Make it work like a matrix stack?
public interface IPoseSetup
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
		public void getTransform( Mat4f dst ) {
			dst.setIdentity();
		}
		
		@Override
		public void applyTransform( Mat4f dst ) {
			// Pass.
		}
		
		@Override
		public void glApply() {
			// Pass.
		}
	};
	
	void getPos( Vec3f dst );
	
	void getRot( Quat4f dst );
	
	float getFactor();
	
	void getTransform( Mat4f dst );
	
	void applyTransform( Mat4f dst );
	
	void glApply();
	
	
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
			public void getTransform( Mat4f dst )
			{
				dst.setIdentity();
				dst.translate( pos );
				dst.rotate( rot );
			}
			
			@Override
			public void applyTransform( Mat4f dst )
			{
				dst.translate( pos );
				dst.rotate( rot );
			}
			
			@Override
			public void glApply()
			{
				GLUtil.glTranslateV3f( pos );
				GLUtil.glRotateQ4f( rot );
			}
		};
	}
	
	static IPoseSetup of( Mat4f src )
	{
		return new IPoseSetup() {
			@Override
			public void getPos( Vec3f dst )
			{
				// TODO: directly get col from mat.
				dst.setZero();
				src.transformAsPoint( dst, dst );
			}
			
			@Override
			public void getRot( Quat4f dst ) {
				dst.set( src );
			}
			
			@Override
			public float getFactor() {
				return 0.0F;
			}
			
			@Override
			public void getTransform( Mat4f dst ) {
				dst.set( src );
			}
			
			@Override
			public void applyTransform( Mat4f dst ) {
				dst.mul( src );
			}
			
			@Override
			public void glApply() {
				GLUtil.glMultMatrix( src );
			}
		};
	}
	
	static IPoseSetup compose( IPoseSetup left, IPoseSetup right )
	{
		return new IPoseSetup() {
			@Override
			public void getPos( Vec3f dst )
			{
				left.getPos( dst );
				
				final Quat4f quat = Quat4f.allocate();
				right.getRot( quat );
				quat.transform( dst, dst );
				Quat4f.release( quat );
				
				final Vec3f vec = Vec3f.allocate();
				right.getPos( vec );
				dst.add( vec );
				Vec3f.release( vec );
			}
			
			@Override
			public void getRot( Quat4f dst )
			{
				left.getRot( dst );
				final Quat4f quat = Quat4f.allocate();
				right.getRot( quat );
				dst.mul( dst, quat );
				Quat4f.release( quat );
			}
			
			@Override
			public float getFactor() {
				return left.getFactor() * right.getFactor();
			}
			
			@Override
			public void getTransform( Mat4f dst )
			{
				right.getTransform( dst );
				left.applyTransform( dst );
			}
			
			@Override
			public void applyTransform( Mat4f dst )
			{
				right.applyTransform( dst );
				left.applyTransform( dst );
			}
			
			@Override
			public void glApply()
			{
				right.glApply();
				left.glApply();
			}
		};
	}
}
