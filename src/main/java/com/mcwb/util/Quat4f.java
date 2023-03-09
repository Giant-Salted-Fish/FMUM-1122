package com.mcwb.util;

import com.mcwb.common.MCWB;
import com.mcwb.devtool.Dev;

@SuppressWarnings( "serial" )
public final class Quat4f extends javax.vecmath.Quat4f implements IReleasable
{
	/**
	 * Use this but NEVER change its value!!!
	 */
	public static final Quat4f ORIGIN = new Quat4f();
	
	private static final ObjPool< Quat4f > POOL = new ObjPool<>( Quat4f::new );
	private static int count = Dev.REFER;
	
	public static Quat4f locate()
	{
		if( ++count > 64 ) MCWB.MOD.error( "count quat over 64! could be something wrong!" );
		return POOL.poll();
	}
	
	/**
	 * @see #Quat4f(float, float, float)
	 */
	public Quat4f( Vec3f eulerRot ) { this.set( eulerRot ); }
	
	/**
	 * Create a quaternion with (x,y,z,w) to be (0,0,0,1)
	 */
	public Quat4f() { super( 0F, 0F, 0F, 1F ); }
	
	/**
	 * Initialize this quaternion with given euler rotation applied in order ZXY
	 */
	public Quat4f( float x, float y, float z ) { this.set( x, y, z ); }
	
	public Quat4f( AngleAxis4f rot ) { this.set( rot ); }
	
	public Quat4f( float x, float y, float z, float w ) { super( x, y, z, w ); }
	
	public Quat4f( Mat4f mat ) { this.set( mat ); }
	
	/**
	 * @see #set(float, float, float)
	 */
	public void set( Vec3f eulerRot ) { this.set( eulerRot.x, eulerRot.y, eulerRot.z ); }
	
	/**
	 * Set this quaternion with corresponding euler rotation applied in order ZXY
	 */
	public void set( float x, float y, float z )
	{
		final Mat4f mat = Mat4f.locate();
		mat.setIdentity();
		mat.eulerRotateYXZ( x, y, z );
		this.set( mat );
		mat.release();
	}
	
	/**
	 * Reset this quaternion to (0,0,0,1)
	 */
	public void clearRot() { this.set( ORIGIN ); }
	
	public void scaleAngle( float scale ) { this.interpolate( ORIGIN, this, scale ); }
	
	@Override
	public void release()
	{
		if( --count < 0 ) MCWB.MOD.error( "count quat below 0! could be something wrong!" );
		POOL.back( this );
	}
}
