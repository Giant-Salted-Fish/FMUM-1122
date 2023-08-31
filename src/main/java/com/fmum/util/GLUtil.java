package com.fmum.util;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

@SideOnly( Side.CLIENT )
public final class GLUtil
{
	private static final FloatBuffer float_buffer = BufferUtils.createFloatBuffer( 16 );
	
	/**
	 * Currently not thread safe.
	 */
	public static void glMulMatrix( Mat4f mat )
	{
		float_buffer.clear();
		mat.store( float_buffer );
		float_buffer.flip();
		GL11.glMultMatrix( float_buffer );
	}
	
	public static void glTranslatef( Vec3f trans ) {
		GL11.glTranslatef( trans.x, trans.y, trans.z );
	}
	
	public static void glRotatef( AngleAxis4f rot ) {
		GL11.glRotatef( rot.angle, rot.x, rot.y, rot.z );
	}
	
	public static void glRotateXf( float angle ) {
		GL11.glRotatef( angle, 1.0F, 0.0F, 0.0F );
	}
	
	public static void glRotateYf( float angle ) {
		GL11.glRotatef( angle, 0.0F, 1.0F, 0.0F );
	}
	
	public static void glRotateZf( float angle ) {
		GL11.glRotatef( angle, 0.0F, 0.0F, 1.0F );
	}
	
	public static void glEulerRotateYXZ( Vec3f rot )
	{
		GL11.glRotatef( rot.y, 0F, 1F, 0F );
		GL11.glRotatef( rot.x, 1F, 0F, 0F );
		GL11.glRotatef( rot.z, 0F, 0F, 1F );
	}
	
	public static void glScalef( float scale ) {
		GL11.glScalef( scale, scale, scale );
	}
	
	private GLUtil() { }
}
