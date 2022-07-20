package com.fmum.common.util;

import org.lwjgl.opengl.GL11;

/**
 * A kind of hack version of matrix4 with all transformations changed to circular functions. Can be
 * used to track coordinates in 3D space(usually used to track the position transformed by OpenGL
 * translate and rotate functions).
 * 
 * @author Giant_Salted_Fish
 */
public class CoordSystem implements Releasable
{
	private static final ObjPool< CoordSystem > pool = new ObjPool<>( () -> new CoordSystem() );
	
	public static final byte
		X = 0,
		Y = 1,
		Z = 2,
		NORM_X = 0,
		NORM_Y = 3,
		NORM_Z = 6,
		OFFSET = 9,
		SUBR_X = 12,
		SUBR_Y = 15,
		SUBR_Z = 18,
		MAT_NORM = 0,
		MAT_SUBR = 12;
	
	private static final byte VEC_LEN = 21;
	
	/**
	 * 0-2: x normal vector
	 * 3-5: y normal vector
	 * 6-8: z normal vector
	 * 9-11: offset vector
	 * 12-14: x sub-rotate vector
	 * 15-17: y sub-rotate vector
	 * 18-20: z sub-rotate vector
	 */
	public final double[] vec = new double[ VEC_LEN ];
	
	public static CoordSystem locate() { return pool.poll(); }
	
	protected CoordSystem() { }
	
	/**
	 * Set offset vector as zero vector. Load identity matrix for normal vectors and sub-rotate
	 * matrix.
	 */
	public final CoordSystem reset()
	{
		this.vec[ OFFSET + X ]
			= this.vec[ OFFSET + Y ]
			= this.vec[ OFFSET + Z ]
			= 0D;
		this.loadIdentity( MAT_NORM );
		this.loadIdentity( MAT_SUBR );
		return this;
	}
	
	/**
	 * Load identity matrix for given matrix
	 * 
	 * @param base
	 *     Matrix to load identity matrix. Should be one of the {@link #MAT_NORM} or
	 *     {@link #MAT_SUBR}.
	 */
	public final CoordSystem loadIdentity( byte base )
	{
		this.vec[ base + Y ] = this.vec[ base + Z ]
			= this.vec[ base + NORM_Y - NORM_X + X ]
			= this.vec[ base + NORM_Y - NORM_X + Z ]
			= this.vec[ base + NORM_Z - NORM_X + X ]
			= this.vec[ base + NORM_Z - NORM_X + Y ]
			= 0D;
		this.vec[ base + X ]
			= this.vec[ base + NORM_Y - NORM_X + Y ]
			= this.vec[ base + NORM_Z - NORM_X + Z ]
			= 1D;
		return this;
	}
	
	/**
	 * Translate current coordinate system with given xyz
	 * 
	 * @param x Amount to translate on X
	 * @param y Amount to translate on Y
	 * @param z Amount to translate on Z
	 */
	public final CoordSystem trans( double x, double y, double z )
	{
		this.trans( x, NORM_X );
		this.trans( y, NORM_Y );
		this.trans( z, NORM_Z );
		return this;
	}

	/**
	 * Translate current coordinate system with given xyz
	 * 
	 * @param vec Amount to translate on each dimension
	 */
	public final CoordSystem trans( Vec3 vec )
	{
		this.trans( vec.x, NORM_X );
		this.trans( vec.y, NORM_Y );
		this.trans( vec.z, NORM_Z );
		return this;
	}
	
	/**
	 * Translate current coordinate system on required axis
	 * 
	 * @param amount Amount to translate
	 * @param base Axis to translate along. One of {@link #NORM_X}, {@link #NORM_Y}, {@link #NORM_Z}
	 */
	public final CoordSystem trans( double amount, byte base )
	{
		this.vec[ OFFSET + X ] += amount * this.vec[ base + X ];
		this.vec[ OFFSET + Y ] += amount * this.vec[ base + Y ];
		this.vec[ OFFSET + Z ] += amount * this.vec[ base + Z ];
		return this;
	}
	
	/**
	 * Translate the coordinate regardless of the current rotation
	 * 
	 * @note Raw translate will not effected by previous scaling
	 * 
	 * @param x Amount to translate on X
	 * @param y Amount to translate on Y
	 * @param z Amount to translate on Z
	 */
	public final CoordSystem globalTrans( double x, double y, double z )
	{
		this.vec[ OFFSET + X ] += x;
		this.vec[ OFFSET + Y ] += y;
		this.vec[ OFFSET + Z ] += z;
		return this;
	}

	/**
	 * Translate the coordinate regardless of the current rotation
	 * 
	 * @note Raw translate will not effected by previous scaling
	 * 
	 * @param vec Amount to translate on each dimension
	 */
	public final CoordSystem globalTrans( Vec3 vec )
	{
		this.vec[ OFFSET + X ] += vec.x;
		this.vec[ OFFSET + Y ] += vec.y;
		this.vec[ OFFSET + Z ] += vec.z;
		return this;
	}
	
	/**
	 * Rotate current coordinate system with given xzy value. It will call {@link #rot(double, byte)}
	 * in x, z, y order.
	 * 
	 * @note Sequence can effect the final result. Correct order should be a reverse of the OpenGL.
	 * @see #rot(double, byte)
	 * 
	 * @param x Amount to rot on X
	 * @param y Amount to rot on Y
	 * @param z Amount to rot on Z
	 */
	public final CoordSystem rot( double x, double y, double z )
	{
		this.rot( x, X );
		this.rot( z, Z );
		this.rot( y, Y );
		return this;
	}
	
	/**
	 * @see #rot(double, double, double)
	 * @param vec Amount to rot on each axis
	 */
	public final CoordSystem rot( Vec3 vec )
	{
		this.rot( vec.x, X );
		this.rot( vec.z, Z );
		this.rot( vec.y, Y );
		return this;
	}
	
	/**
	 * <p> Rotate current coordinate system on required axis. </p>
	 * 
	 * <p> More specifically, it rotates the temporary sub-coordinate system rather than the global
	 * coordinate system. Calling {@link #submitRot()} will merge the effect of sub-coordinate
	 * system into main coordinate system. To have the identical behavior, call {@link #submitRot()}
	 * before applying any translation if any rotation has been applied and yet {@link #submitRot()}
	 * is not called. </p>
	 * 
	 * @note
	 *     Sequence of the rotations applied can effect the final result. It should be called in a
	 *     reversed order of OpenGL.
	 * @see #globalRot(double, byte)
	 * 
	 * @param amount Amount to rotate on this axis
	 * @param along Axis to rotate with. One of {@link #X}, {@link #Y}, {@link #Z}.
	 */
	public final CoordSystem rot( double amount, byte along )
	{
		this.rot( amount, along, SUBR_X );
		this.rot( amount, along, SUBR_Y );
		this.rot( amount, along, SUBR_Z );
		return this;
	}
	
	/**
	 * Apply the effect of the rotations currently applied
	 * 
	 * @note
	 *     that for OpenGL this should be called after applying all rotations and going to apply
	 *     next translation
	 */
	public final CoordSystem submitRot()
	{
		this.submitRot( SUBR_X );
		this.submitRot( SUBR_Y );
		this.submitRot( SUBR_Z );
		this.copy( SUBR_X, NORM_X );
		this.copy( SUBR_Y, NORM_Y );
		this.copy( SUBR_Z, NORM_Z );
		this.loadIdentity( MAT_SUBR );
		return this;
	}

	/**
	 * Rotate normal vectors of current coordinate system with given xzy value. It will call
	 * {@link #globalRot(double, byte)} in x, z, y order.
	 * 
	 * @note Sequence can effect the final result. Correct order should be a reverse of the OpenGL.
	 * @see #globalRot(double, byte)
	 * 
	 * @param x Amount to rot on X
	 * @param y Amount to rot on Y
	 * @param z Amount to rot on Z
	 */
	public final CoordSystem globalRot( double x, double y, double z )
	{
		this.globalRot( x, X );
		this.globalRot( z, Z );
		this.globalRot( y, Y );
		return this;
	}
	
	/**
	 * @see #globalRot(double, double, double)
	 * @param vec Amount to rot on each axis
	 */
	public final CoordSystem globalRot( Vec3 vec )
	{
		this.globalRot( vec.x, X );
		this.globalRot( vec.z, Z );
		this.globalRot( vec.y, Y );
		return this;
	}
	
	/**
	 * <p> Rotate global coordinate system on required axis. </p>
	 * 
	 * <p> More specifically, it rotates the main coordinate system rather than temporary
	 * sub-coordinate system. It does not require to call {@link #submitRot()} to apply the effect.
	 * Make sure you know the effects before calling this function. </p>
	 * 
	 * @note
	 *     Sequence of the rotations applied can effect the final result. It should be called in a
	 *     reversed order of OpenGL.
	 * @see #rot(double, byte)
	 * 
	 * @param amount Amount to rotate on this axis
	 * @param along Axis to rotate with. One of {@link #X}, {@link #Y}, {@link #Z}.
	 */
	public final CoordSystem globalRot( double amount, byte along )
	{
		this.rot( amount, along, NORM_X );
		this.rot( amount, along, NORM_Y );
		this.rot( amount, along, NORM_Z );
		return this;
	}
	
	/**
	 * Scale current coordinate system for given amount
	 * 
	 * @param x Amount to scale on X
	 * @param y Amount to scale on Y
	 * @param z Amount to scale on Z
	 */
	public final CoordSystem scale( double x, double y, double z )
	{
		this.scale( x, NORM_X );
		this.scale( y, NORM_Y );
		this.scale( z, NORM_Z );
		return this;
	}
	
	/**
	 * Scale current coordinate system on required axis
	 * 
	 * @param amount Amount to scale
	 * @param base Normal vector to scale
	 */
	public final CoordSystem scale( double amount, byte base )
	{
		this.vec[ base + X ] *= amount;
		this.vec[ base + Y ] *= amount;
		this.vec[ base + Z ] *= amount;
		return this;
	}
	
	/**
	 * Apply rotation, translation and scale of this coordinate system for the given raw vector
	 * 
	 * @param raw Raw vector to transfer
	 * @param dst Destination of the result
	 */
	public final Vec3 apply( Vec3 raw, Vec3 dst )
	{
		// Destination vector could be raw vector
		// So buffer result before computation is done
		double x = (
			raw.x * this.vec[ NORM_X + X ]
			+ raw.y * this.vec[ NORM_Y + X ]
			+ raw.z * this.vec[ NORM_Z + X ]
			+ this.vec[ OFFSET + X ]
		);
		double y = (
			raw.x * this.vec[ NORM_X + Y ]
			+ raw.y * this.vec[ NORM_Y + Y ]
			+ raw.z * this.vec[ NORM_Z + Y ]
			+ this.vec[ OFFSET + Y ]
		);
		dst.z = (
			raw.x * this.vec[ NORM_X + Z ]
			+ raw.y * this.vec[ NORM_Y + Z ]
			+ raw.z * this.vec[ NORM_Z + Z ]
			+ this.vec[ OFFSET + Z ]
		);
		dst.y = y;
		dst.x = x;
		return dst;
	}
	
	/**
	 * Convenience method used in processing ToolBox models
	 */
	public final Vec3f apply( Vec3f raw, Vec3f dst )
	{
		float x = ( float ) (
			raw.x * this.vec[ NORM_X + X ]
			+ raw.y * this.vec[ NORM_Y + X ]
			+ raw.z * this.vec[ NORM_Z + X ]
			+ this.vec[ OFFSET + X ]
		);
		float y = ( float ) (
			raw.x * this.vec[ NORM_X + Y ]
			+ raw.y * this.vec[ NORM_Y + Y ]
			+ raw.z * this.vec[ NORM_Z + Y ]
			+ this.vec[ OFFSET + Y ]
		);
		dst.z = ( float ) (
			raw.x * this.vec[ NORM_X + Z ]
			+ raw.y * this.vec[ NORM_Y + Z ]
			+ raw.z * this.vec[ NORM_Z + Z ]
			+ this.vec[ OFFSET + Z ]
		);
		dst.y = y;
		dst.x = x;
		return dst;
	}
	
	/**
	 * Apply rotation and scale of this coordinate system for the given raw vector
	 * 
	 * @param raw Raw vector to transfer
	 * @param dst Destination of the result
	 */
	public final Vec3 applyRot( Vec3 raw, Vec3 dst )
	{
		double x = (
			raw.x * this.vec[ NORM_X + X ]
			+ raw.y * this.vec[ NORM_Y + X ]
			+ raw.z * this.vec[ NORM_Z + X ]
		);
		double y = (
			raw.x * this.vec[ NORM_X + Y ]
			+ raw.y * this.vec[ NORM_Y + Y ]
			+ raw.z * this.vec[ NORM_Z + Y ]
		);
		dst.z = (
			raw.x * this.vec[ NORM_X + Z ]
			+ raw.y * this.vec[ NORM_Y + Z ]
			+ raw.z * this.vec[ NORM_Z + Z ]
		);
		dst.y = y;
		dst.x = x;
		return dst;
	}
	
	/**
	 * Apply rotation, translation and scale of this coordinate system for the given raw system
	 * 
	 * @param raw Sub-coordinate system
	 * @param dst Destination coordinate system. Should not be this instance.
	 * @param opVec Vector that will be used in operation
	 */
	public final CoordSystem apply( CoordSystem raw, CoordSystem dst, Vec3 opVec )
	{
		raw.get( opVec, NORM_X );
		this.applyRot( opVec, opVec );
		dst.set( opVec, NORM_X );
		raw.get( opVec, NORM_Y );
		this.applyRot( opVec, opVec );
		dst.set( opVec, NORM_Y );
		raw.get( opVec, NORM_Z );
		this.applyRot( opVec, opVec );
		dst.set( opVec, NORM_Z );
		
		raw.get( opVec, OFFSET );
		this.apply( opVec, opVec );
		dst.set( opVec, OFFSET );
		return dst;
	}
	
	/**
	 * <p> Get xzy angle of the system. You can call
	 * {@link GL11#glRotated(double, double, double, double)} in YZX order to reproduce
	 * the angles. </p>
	 * 
	 * <p> Note that this method will devastate current rotation of the system. Make sure you set it
	 * back if you want to keep to use the angle information in this system. </p>
	 * 
	 * @param dst Destination vector to receive angle values
	 */
	public final Vec3 getAngle( Vec3 dst )
	{
		double sin, cos;
		
		sin = Math.toDegrees(
			Math.atan( -this.vec[ NORM_X + Z ] / ( cos = this.vec[ NORM_X + X ] ) )
		);
		// Check angle flip of tan function
		this.globalRot( -( sin = cos < 0D ? sin + 180D : sin ), Y );
		dst.y = sin;
		
		sin = Math.toDegrees(
			Math.atan( this.vec[ NORM_X + Y ] / ( cos = this.vec[ NORM_X + X ] ) )
		);
		this.globalRot( -( sin = cos < 0D ? sin + 180D : sin ), Z );
		dst.z = sin;
		
		sin = Math.toDegrees(
			Math.atan( this.vec[ NORM_Y + Z ] / ( cos = this.vec[ NORM_Y + Y ] ) )
		);
		dst.x = cos < 0D ? sin + 180D : sin;
		return dst;
	}
	
	/**
	 * <p> Get zy angle of the system. It can be considered as the view rotation of the system. You
	 * can call {@link GL11#glRotated(double, double, double, double)} in YZ order
	 * to reproduce the angles. </p>
	 * 
	 * <p> Note that this method uses normal x to calculate angle and will devastate normal x, which
	 * means it is not necessary to set normal y and normal z before calling this method and normal
	 * x should not be trusted after calling this method. </p>
	 * 
	 * @param dst Destination vector to receive angle values. Only y and z will be used.
	 */
	public final Vec3 getViewAngle( Vec3 dst )
	{
		double sin = this.vec[ NORM_X + Z ];
		double cos = this.vec[ NORM_X + X ];
		
		sin = (
			cos != 0D
			? Math.toDegrees( Math.atan( -sin / cos ) )
			: sin > 0D ? -90D : sin < 0D ? 90D : 0D
		);
		// Check angle flip of tan function
		this.globalRot( -( sin = cos < 0D ? sin + 180D : sin ), Y );
		dst.y = sin;
		
		// Now cos value can only be positive or zero
		dst.z = (
			( cos = this.vec[ NORM_X + X ] ) != 0D
			? Math.toDegrees( Math.atan( this.vec[ NORM_X + Y ] / cos ) )
			: this.vec[ NORM_X + Y ] > 0D ? 90D : -90D
		);
		return dst;
	}
	
	/**
	 * <p> Get x angle of the system. It can be considered as the camera roll of the system. </p>
	 * 
	 * <p> Note that this method uses normal y to calculate angle, which means it is not necessary
	 * to set normal x and normal z before calling this method. </p>
	 * 
	 * @return Angle of the system along x-axis
	 */
	public final double getCameraRoll()
	{
		double sin = this.vec[ NORM_Y + Z ];
		double cos = this.vec[ NORM_Y + Y ];
		
		sin = (
			cos != 0D
			? Math.toDegrees( Math.atan( sin / cos ) )
			: sin > 0D ? 90D : sin < 0D ? -90D : 0D
		);
		return cos < 0D ? sin + 180D : sin;
	}
	
	/**
	 * Copy the value of required vector to given destination
	 * 
	 * @param dst Destination vector
	 * @param base Required value source
	 */
	public final Vec3 get( Vec3 dst, byte base )
	{
		dst.x = this.vec[ base + X ];
		dst.y = this.vec[ base + Y ];
		dst.z = this.vec[ base + Z ];
		return dst;
	}
	
	public final CoordSystem set( double x, double y, double z, byte base )
	{
		this.vec[ base + X ] = x;
		this.vec[ base + Y ] = y;
		this.vec[ base + Z ] = z;
		return this;
	}
	
	public final CoordSystem set( Vec3 v, byte base )
	{
		this.vec[ base + X ] = v.x;
		this.vec[ base + Y ] = v.y;
		this.vec[ base + Z ] = v.z;
		return this;
	}
	
	/**
	 * Copy value from source system to this instance
	 * 
	 * @param src Value provider
	 */
	public final CoordSystem set( CoordSystem src )
	{
		for( int i = VEC_LEN; i-- > 0; this.vec[ i ] = src.vec[ i ] );
		return this;
	}
	
	@Override
	public String toString()
	{
		return(
			"Coordinate system ["
			+ "norx(" + this.vec[ NORM_X + X ] + ", "
			+ this.vec[ NORM_X + Y ] + ", "
			+ this.vec[ NORM_X + Z ] + ") "
			+ "nory(" + this.vec[NORM_Y + X ] + ", "
			+ this.vec[ NORM_Y + Y ] + ", "
			+ this.vec[ NORM_Y + Z ] + ") "
			+ "norz(" + this.vec[NORM_Z + X ] + ", "
			+ this.vec[ NORM_Z + Y ] + ", "
			+ this.vec[ NORM_Z + Z ] + ") "
			+ "offs(" + this.vec[OFFSET + X ] + ", "
			+ this.vec[ OFFSET + Y ] + ", "
			+ this.vec[ OFFSET + Z ] + ")]"
		);
	}
	
	@Override
	public void release() { pool.back( this ); }
	
	private void rot( double amount, byte along, byte base )
	{
		// Get sin and cos
		double sin = Math.toRadians( amount );
		double cos = Math.cos( sin );
		sin = Math.sin( sin );
		
		// Setup transform based on axis
		switch( along )
		{
		case X:
			amount = this.vec[ base + Y ] * cos - this.vec[ base + Z ] * sin;
			this.vec[ base + Z ]
				= this.vec[ base + Y ] * sin + this.vec[ base + Z ] * cos;
			this.vec[ base + Y] = amount;
			break;
		case Y:
			amount = this.vec[ base + Z ] * sin + this.vec[ base + X ] * cos;
			this.vec[ base + Z ]
				= this.vec[ base + Z ] * cos - this.vec[ base + X ] * sin;
			this.vec[ base + X ] = amount;
			break;
		case Z:
			amount = this.vec[ base + X ] * cos - this.vec[ base + Y ] * sin;
			this.vec[ base + Y ]
				= this.vec[ base + X ] * sin + this.vec[ base + Y ] * cos;
			this.vec[ base + X ] = amount;
			break;
		}
	}
	
	private void submitRot( byte base )
	{
		double x =
			this.vec[ base + X ] * this.vec[ NORM_X + X ]
			+ this.vec[ base + Y ] * this.vec[ NORM_Y + X ]
			+ this.vec[ base + Z ] * this.vec[ NORM_Z + X ];
		double y =
			this.vec[ base + X ] * this.vec[ NORM_X + Y ]
			+ this.vec[ base + Y ] * this.vec[ NORM_Y + Y ]
			+ this.vec[ base + Z ] * this.vec[ NORM_Z + Y ];
		this.vec[base + Z] =
			this.vec[ base + X ] * this.vec[ NORM_X + Z ]
			+ this.vec[ base + Y ] * this.vec[ NORM_Y + Z ]
			+ this.vec[ base + Z ] * this.vec[ NORM_Z + Z ];
		this.vec[ base + Y ] = y;
		this.vec[ base + X ] = x;
	}
	
	private void copy( byte from, byte to )
	{
		this.vec[ to + X ] = this.vec[ from + X ];
		this.vec[ to + Y ] = this.vec[ from + Y ];
		this.vec[ to + Z ] = this.vec[ from + Z ];
	}
}
