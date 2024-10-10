package gsf.util.render;

import gsf.util.math.Mat4f;
import gsf.util.math.Quat4f;
import gsf.util.math.Vec3f;

import javax.vecmath.Matrix4f;

public interface IPose
{
	IPose EMPTY = new IPose() {
		@Override
		public void getPos( Vec3f dst ) {
			dst.setZero();
		}
		
		@Override
		public void getRot( Quat4f dst ) {
			dst.clearRot();
		}
		
		@Override
		public void transform( Vec3f point, Vec3f dst ) {
			// Pass.
		}
		
		@Override
		public void glApply() {
			// Pass.
		}
	};
	
	
	void getPos( Vec3f dst );
	
	void getRot( Quat4f dst );
	
	default void transform( Vec3f point, Vec3f dst )
	{
		final Quat4f rot = Quat4f.allocate();
		this.getRot( rot );
		rot.transform( point, dst );
		Quat4f.release( rot );
		
		final Vec3f pos = Vec3f.allocate();
		this.getPos( pos );
		dst.add( pos );
		Vec3f.release( pos );
	}
	
	default void glApply()
	{
		final Vec3f pos = Vec3f.allocate();
		this.getPos( pos );
		GLUtil.glTranslateV3f( pos );
		Vec3f.release( pos );
		
		final Quat4f rot = Quat4f.allocate();
		this.getRot( rot );
		GLUtil.glRotateQ4f( rot );
		Quat4f.release( rot );
	}
	
	
	static IPose ofPos( Vec3f pos )
	{
		return new IPose() {
			@Override
			public void getPos( Vec3f dst ) {
				dst.set( pos );
			}
			
			@Override
			public void getRot( Quat4f dst ) {
				dst.clearRot();
			}
			
			@Override
			public void transform( Vec3f point, Vec3f dst ) {
				dst.add( point, pos );
			}
			
			@Override
			public void glApply() {
				GLUtil.glTranslateV3f( pos );
			}
		};
	}
	
	static IPose ofRot( Quat4f rot )
	{
		return new IPose() {
			@Override
			public void getPos( Vec3f dst ) {
				dst.setZero();
			}
			
			@Override
			public void getRot( Quat4f dst ) {
				dst.set( rot );
			}
			
			@Override
			public void transform( Vec3f point, Vec3f dst ) {
				rot.transform( point, dst );
			}
			
			@Override
			public void glApply() {
				GLUtil.glRotateQ4f( rot );
			}
		};
	}
	
	static IPose of( Vec3f pos, Quat4f rot )
	{
		return new IPose() {
			@Override
			public void getPos( Vec3f dst ) {
				dst.set( pos );
			}
			
			@Override
			public void getRot( Quat4f dst ) {
				dst.set( rot );
			}
			
			@Override
			public void transform( Vec3f point, Vec3f dst )
			{
				rot.transform( point, dst );
				dst.add( pos );
			}
			
			@Override
			public void glApply()
			{
				GLUtil.glTranslateV3f( pos );
				GLUtil.glRotateQ4f( rot );
			}
		};
	}
	
	static IPose ofMat( Mat4f mat )
	{
		return new IPose() {
			@Override
			public void getPos( Vec3f dst )
			{
				// TODO: directly get col from mat.
				dst.setZero();
				mat.transformAsPoint( dst, dst );
			}
			
			@Override
			public void getRot( Quat4f dst ) {
				dst.set( mat );
			}
			
			@Override
			public void transform( Vec3f point, Vec3f dst ) {
				mat.transformAsPoint( point, dst );
			}
			
			@Override
			public void glApply() {
				GLUtil.glMultMatrix( mat );
			}
		};
	}
	
	/**
	 * @see Mat4f#mul(Matrix4f, Matrix4f)
	 */
	static IPose compose( IPose left, IPose right )
	{
		final Vec3f pos = new Vec3f();
		final Quat4f rot = new Quat4f();
		right.getPos( pos );
		left.getRot( rot );
		rot.transform( pos, pos );
		
		final Vec3f vec = Vec3f.allocate();
		left.getPos( vec );
		pos.add( vec );
		Vec3f.release( vec );
		
		final Quat4f quat = Quat4f.allocate();
		right.getRot( quat );
		rot.mul( rot, quat );
		Quat4f.release( quat );
		
		return of( pos, rot );
	}
}
