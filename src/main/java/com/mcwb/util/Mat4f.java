package com.mcwb.util;

import java.nio.FloatBuffer;

import javax.vecmath.Matrix4f;

import org.lwjgl.opengl.GL11;

import com.mcwb.common.MCWB;
import com.mcwb.devtool.Dev;

import net.minecraft.util.math.MathHelper;

@SuppressWarnings( "serial" )
public final class Mat4f extends Matrix4f implements IReleasable
{
	private static final ObjPool< Mat4f > POOL = new ObjPool<>( Mat4f::new );
	private static int count = Dev.REFER;
	
	// FIXME: count
	public static Mat4f locate()
	{
		if( ++count > 64 ) MCWB.MOD.error( "count mat over 64! could be something wrong!" );
		return POOL.poll();
	}
	
	public void store( FloatBuffer buf )
	{
		buf.put( this.m00 );
		buf.put( this.m10 );
		buf.put( this.m20 );
		buf.put( this.m30 );
		
		buf.put( this.m01 );
		buf.put( this.m11 );
		buf.put( this.m21 );
		buf.put( this.m31 );
		
		buf.put( this.m02 );
		buf.put( this.m12 );
		buf.put( this.m22 );
		buf.put( this.m32 );
		
		buf.put( this.m03 );
		buf.put( this.m13 );
		buf.put( this.m23 );
		buf.put( this.m33 );
	}
	
	public void storeTranspose( FloatBuffer buf )
	{
		buf.put( this.m00 );
		buf.put( this.m01 );
		buf.put( this.m02 );
		buf.put( this.m03 );
		
		buf.put( this.m10 );
		buf.put( this.m11 );
		buf.put( this.m12 );
		buf.put( this.m13 );
		
		buf.put( this.m20 );
		buf.put( this.m21 );
		buf.put( this.m22 );
		buf.put( this.m23 );
		
		buf.put( this.m30 );
		buf.put( this.m31 );
		buf.put( this.m32 );
		buf.put( this.m33 );
	}
	
	public void translate( Vec3f vec ) { this.translate( vec.x, vec.y, vec.z ); }
	
	public void translate( float x, float y, float z )
	{
		this.m03 += this.m00 * x + this.m01 * y + this.m02 * z;
		this.m13 += this.m10 * x + this.m11 * y + this.m12 * z;
		this.m23 += this.m20 * x + this.m21 * y + this.m22 * z;
//		this.m33 += this.m30 * x + this.m31 * y + this.m32 * z; // TODO: test this again
	}
	
	// TODO: a better implementation?
	public void rotate( Quat4f quat )
	{
		final Mat4f mat = locate();
		mat.set( quat );
		this.mul( mat );
		mat.release();
	}
	
	/**
	 * Axis need to be normalized
	 * 
	 * @param angle The angle to rotate about the axis in degrees
	 */
	public void rotate( float angle, Vec3f axis ) {
		this.rotate( angle, axis.x, axis.y, axis.z );
	}
	
	/**
	 * Axis need to be normalized
	 * 
	 * @param angle The angle to rotate about the axis in degrees
	 */
	public void rotate( float angle, float ax, float ay, float az )
	{
		final float xz = ax * az;
		final float xy = ax * ay;
		final float yz = ay * az;
		
		final float radians = angle * Util.TO_RADIANS;
		final float sinTheta = MathHelper.sin( radians );
		final float cosTheta = MathHelper.cos( radians );
		final float t = 1F - cosTheta;
		
		final float rm00 = t * ax * ax + cosTheta;
		final float rm01 = t * xy - sinTheta * az;
		final float rm02 = t * xz + sinTheta * ay;
		final float rm10 = t * xy + sinTheta * az;
		final float rm11 = t * ay * ay + cosTheta;
		final float rm12 = t * yz - sinTheta * ax;
		final float rm20 = t * xz - sinTheta * ay;
		final float rm21 = t * yz + sinTheta * ax;
		final float rm22 = t * az * az + cosTheta;
		
		// Multiply!
		final float mm00 = this.m00 * rm00 + this.m01 * rm10 + this.m02 * rm20;
		final float mm01 = this.m00 * rm01 + this.m01 * rm11 + this.m02 * rm21;
		final float mm02 = this.m00 * rm02 + this.m01 * rm12 + this.m02 * rm22;
		final float mm10 = this.m10 * rm00 + this.m11 * rm10 + this.m12 * rm20;
		final float mm11 = this.m10 * rm01 + this.m11 * rm11 + this.m12 * rm21;
		final float mm12 = this.m10 * rm02 + this.m11 * rm12 + this.m12 * rm22;
		final float mm20 = this.m20 * rm00 + this.m21 * rm10 + this.m22 * rm20;
		final float mm21 = this.m20 * rm01 + this.m21 * rm11 + this.m22 * rm21;
		final float mm22 = this.m20 * rm02 + this.m21 * rm12 + this.m22 * rm22;
		
		this.m00 = mm00;
		this.m01 = mm01;
		this.m02 = mm02;
		this.m10 = mm10;
		this.m11 = mm11;
		this.m12 = mm12;
		this.m20 = mm20;
		this.m21 = mm21;
		this.m22 = mm22;
	}
	
	/**
	 * @param angle The angle to rotate about the X axis in degrees
	 */
	public void rotateX( float angle )
	{
		final float radians = angle * Util.TO_RADIANS;
		final float sin = MathHelper.sin( radians );
		final float cos = MathHelper.cos( radians );
		
		final float dm01 = this.m01 *  cos + this.m02 * sin;
		final float dm02 = this.m01 * -sin + this.m02 * cos;
		final float dm11 = this.m11 *  cos + this.m12 * sin;
		final float dm12 = this.m11 * -sin + this.m12 * cos;
		final float dm21 = this.m21 *  cos + this.m22 * sin;
		final float dm22 = this.m21 * -sin + this.m22 * cos;
		
		this.m01 = dm01;
		this.m02 = dm02;
		this.m11 = dm11;
		this.m12 = dm12;
		this.m21 = dm21;
		this.m22 = dm22;
	}
	
	/**
	 * @param angle The angle to rotate about the Y axis in degrees
	 */
	public void rotateY( float angle )
	{
		final float radians = angle * Util.TO_RADIANS;
		final float sin = MathHelper.sin( radians );
		final float cos = MathHelper.cos( radians );
		
		final float dm00 = this.m00 * cos + this.m02 * -sin;
		final float dm02 = this.m00 * sin + this.m02 *  cos;
		final float dm10 = this.m10 * cos + this.m12 * -sin;
		final float dm12 = this.m10 * sin + this.m12 *  cos;
		final float dm20 = this.m20 * cos + this.m22 * -sin;
		final float dm22 = this.m20 * sin + this.m22 *  cos;
		
		this.m00 = dm00;
		this.m02 = dm02;
		this.m10 = dm10;
		this.m12 = dm12;
		this.m20 = dm20;
		this.m22 = dm22;
	}
	
	/**
	 * @param angle The angle to rotate about the Z axis in degrees
	 */
	public void rotateZ( float angle )
	{
		final float radians = angle * Util.TO_RADIANS;
		final float sin = MathHelper.sin( radians );
		final float cos = MathHelper.cos( radians );
		
		final float dm00 = this.m00 *  cos + this.m01 * sin;
		final float dm01 = this.m00 * -sin + this.m01 * cos;
		final float dm10 = this.m10 *  cos + this.m11 * sin;
		final float dm11 = this.m10 * -sin + this.m11 * cos;
		final float dm20 = this.m20 *  cos + this.m21 * sin;
		final float dm21 = this.m20 * -sin + this.m21 * cos;
		
		this.m00 = dm00;
		this.m01 = dm01;
		this.m10 = dm10;
		this.m11 = dm11;
		this.m20 = dm20;
		this.m21 = dm21;
	}
	
	/**
	 * Equivalent as calling {@link GL11#glRotatef(float, float, float, float)} in same order
	 * 
	 * @param x The angle to rotate about the X axis in degrees
	 * @param y The angle to rotate about the Y axis in degrees
	 * @param z The angle to rotate about the Z axis in degrees
	 */
	public void eulerRotateYXZ( float x, float y, float z )
	{
		this.rotateY( y );
		this.rotateX( x );
		this.rotateZ( z );
	}
	
	/**
	 * Equivalent as calling {@link GL11#glRotatef(float, float, float, float)} in same order
	 * 
	 * @param angle The angle to rotate about the three axis in degrees
	 */
	public void eulerRotateYXZ( Vec3f angle )
	{
		this.rotateY( angle.y );
		this.rotateX( angle.x );
		this.rotateZ( angle.z );
	}
	
	public void scale( float x, float y, float z )
	{
		this.m00 *= x;
		this.m10 *= x;
		this.m20 *= x;
		
		this.m01 *= y;
		this.m11 *= y;
		this.m21 *= y;
		
		this.m02 *= z;
		this.m12 *= z;
		this.m22 *= z;
	}
	
	public void scale( float scale )
	{
		this.m00 *= scale;
		this.m01 *= scale;
		this.m02 *= scale;
		
		this.m10 *= scale;
		this.m11 *= scale;
		this.m12 *= scale;
		
		this.m20 *= scale;
		this.m21 *= scale;
		this.m22 *= scale;
	}
	
	/**
	 * Obtain euler angle applied in YXZ order in degrees
	 * 
	 * @note
	 *     {@link #scale(Vec3f)} should not be applied to this matrix. Otherwise, the angle
	 *     obtained via this method could be wrong.
	 * @param dst Angle will be saved into this vector in degrees
	 */
	public final void getEulerAngleYXZ( Vec3f dst )
	{
		dst.set(
			Util.TO_DEGREES * ( float ) -Math.asin( this.m12 ),
			Util.TO_DEGREES * ( float ) Math.atan2( this.m02, this.m22 ),
			Util.TO_DEGREES * ( float ) Math.atan2( this.m10, this.m11 )
		);
	}
	
	/**
	 * @return Z euler angle in YXZ order in degrees
	 */
	public final float getEulerAngleZ() {
		return Util.TO_DEGREES * ( float ) Math.atan2( this.m10, this.m11 );
	}
	
	public final void transformAsPoint( Vec3f point )
	{
		this.transform( point );
		point.x += this.m03;
		point.y += this.m13;
		point.z += this.m23;
	}
	
	@Override
	public void release()
	{
		if( --count < 0 ) MCWB.MOD.error( "count mat below 0! could be something wrong!" );
		POOL.back( this );
	}
}
