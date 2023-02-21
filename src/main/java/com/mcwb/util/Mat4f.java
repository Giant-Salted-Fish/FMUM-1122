package com.mcwb.util;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

/** 
 * @author Giant_Salted_Fish
 */
public class Mat4f extends Matrix4f implements IReleasable
{
	private static final long serialVersionUID = -3852853301881304154L;
	
	public static final Vec3f
		AXIS_X = new Vec3f( 1F, 0F, 0F ),
		AXIS_Y = new Vec3f( 0F, 1F, 0F ),
		AXIS_Z = new Vec3f( 0F, 0F, 1F );
	
	private static final ObjPool< Mat4f > POOL = new ObjPool<>( Mat4f::new );
	
	public static Mat4f locate() { return POOL.poll(); }
	
	public final Mat4f translate( float x, float y, float z )
	{
		this.m30 += this.m00 * x + this.m10 * y + this.m20 * z;
		this.m31 += this.m01 * x + this.m11 * y + this.m21 * z;
		this.m32 += this.m02 * x + this.m12 * y + this.m22 * z;
		this.m33 += this.m03 * x + this.m13 * y + this.m23 * z;
		return this;
	}
	
	/**
	 * @param angle Angle to rotate alone x-axis in degrees
	 * @return {@code this}
	 */
	public final Mat4f rotateX( float angle )
	{
		this.rotate( angle * Util.TO_RADIANS, AXIS_X );
		return this;
	}
	
	/**
	 * @param angle Angle to rotate alone y-axis in degrees
	 * @return {@code this}
	 */
	public final Mat4f rotateY( float angle )
	{
		this.rotate( angle * Util.TO_RADIANS, AXIS_Y );
		return this;
	}
	
	/**
	 * @param angle Angle to rotate alone z-axis in degrees
	 * @return {@code this}
	 */
	public final Mat4f rotateZ( float angle )
	{
		this.rotate( angle * Util.TO_RADIANS, AXIS_Z );
		return this;
	}
	
	/**
	 * Equivalent as calling {@link GL11#glRotatef(float, float, float, float)} in same order
	 * 
	 * @param angle Rotation angle in degrees
	 * @return {@code this}
	 */
	public final Mat4f eulerRotateYXZ( Vector3f angle ) {
		return this.eulerRotateYXZ( angle.x, angle.y, angle.z );
	}
	
	/**
	 * Equivalent as calling {@link GL11#glRotatef(float, float, float, float)} in same order
	 * 
	 * @param x Angle to rotate alone x-axis in degrees
	 * @param y Angle to rotate alone y-axis in degrees
	 * @param z Angle to rotate alone z-axis in degrees
	 * @return {@code this}
	 */
	public final Mat4f eulerRotateYXZ( float x, float y, float z )
	{
		this.rotate( y * Util.TO_RADIANS, AXIS_Y );
		this.rotate( x * Util.TO_RADIANS, AXIS_X );
		this.rotate( z * Util.TO_RADIANS, AXIS_Z );
		return this;
	}
	
	public final Mat4f apply( Vector3f vec ) { return this.apply( vec, vec ); }
	
	/**
	 * Apply transform of this matrix to the given src vector and save the result into dst vector
	 * 
	 * @param src Source vector
	 * @param dst Destination vector
	 * @return {@code this}
	 */
	public final Mat4f apply( Vector3f src, Vector3f dst )
	{
		float x = src.x * this.m00 + src.y * this.m10 + src.z * this.m20 + this.m30;
		float y = src.x * this.m01 + src.y * this.m11 + src.z * this.m21 + this.m31;
		float z = src.x * this.m02 + src.y * this.m12 + src.z * this.m22 + this.m32;
		
		dst.z = z;
		dst.y = y;
		dst.x = x;
		
		return this;
	}
	
	/**
	 * Obtain euler angle applied in YXZ order in degrees
	 * 
	 * @note
	 *     {@link #scale(Vector3f)} should not be applied to this matrix. Otherwise, the angle
	 *     obtained via this method could be wrong.
	 * @param dst Angle will be saved into this vector in degrees
	 * @return {@code this}
	 */
	public final Mat4f getEulerAngleYXZ( Vector3f dst )
	{
		dst.set(
			Util.TO_DEGREES * ( float ) -Math.asin( this.m21 ),
			Util.TO_DEGREES * ( float ) Math.atan2( this.m20, this.m22 ),
			Util.TO_DEGREES * ( float ) Math.atan2( this.m01, this.m11 )
		);
		return this;
	}
	
	public final float getEulerAngleZ() {
		return Util.TO_DEGREES * ( float ) Math.atan2( this.m01, this.m11 );
	}
	
	/**
	 * Obtain translation saved in this matrix
	 * 
	 * @param dst Translation will be saved into this vector
	 * @return {@code this}
	 */
	public final Mat4f getTranslation( Vector3f dst )
	{
		dst.set( this.m30, this.m31, this.m32 );
		return this;
	}
	
	public final Mat4f getBaseVectorX( Vector3f dst )
	{
		dst.x = this.m00;
		dst.y = this.m01;
		dst.z = this.m02;
		return this;
	}
	
	public final Mat4f getBaseVectorY( Vector3f dst )
	{
		dst.x = this.m10;
		dst.y = this.m11;
		dst.z = this.m12;
		return this;
	}
	
	public final Mat4f getBaseVectorZ( Vector3f dst )
	{
		dst.x = this.m20;
		dst.y = this.m21;
		dst.z = this.m22;
		return this;
	}
	
	@Override
	public void release() { POOL.back( this ); }
}
