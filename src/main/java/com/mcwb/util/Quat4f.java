package com.mcwb.util;

import com.mcwb.common.MCWB;
import com.mcwb.devtool.Dev;

@SuppressWarnings( "serial" )
public final class Quat4f extends javax.vecmath.Quat4f implements IReleasable
{
	public static final Quat4f ORIGIN = new Quat4f();
	
	private static final ObjPool< Quat4f > POOL = new ObjPool<>( Quat4f::new );
	private static int count = Dev.REFER;
	
	public static Quat4f locate()
	{
		if( ++count > 64 ) MCWB.MOD.error( "count quat over 64! could be something wrong!" );
		return POOL.poll();
	}
	
	/**
	 * Create a quaternion with (x,y,z,w) to be (0,0,0,1)
	 */
	public Quat4f() { super( 0F, 0F, 0F, 1F ); }
	
	public Quat4f( float x, float y, float z, float w ) { super( x, y, z, w ); }
	
	@Override
	public void release()
	{
		if( --count < 0 ) MCWB.MOD.error( "count quat below 0! could be something wrong!" );
		POOL.back( this );
	}
}
