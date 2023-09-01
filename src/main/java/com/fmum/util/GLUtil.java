package com.fmum.util;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.util.ArrayList;

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
	
	
	// For glow stuff.
	private static float
		lightmap_base_x = 0.0F,
		lightmap_base_y = 0.0F;
	
	private static final ArrayList< Float > glow_stack = new ArrayList<>();
	private static float max_glow = 0.0F;
	
	/**
	 * Call {@link #glowOn(float)} with {@code 1.0F}.
	 */
	public static void maxGlowOn() {
		glowOn( 1.0F );
	}
	
	/**
	 * Call {@link #glowOff()} after you complete light stuff render.
	 *
	 * @param glow_degree Value in range {@code 0.0F-1.0F}.
	 */
	public static void glowOn( float glow_degree )
	{
		if ( glow_stack.isEmpty() )
		{
			// Push light bits and record previous brightness.
			GL11.glPushAttrib( GL11.GL_LIGHTING_BIT );  // TODO: Validate if this is needed.
			lightmap_base_x = OpenGlHelper.lastBrightnessX;
			lightmap_base_y = OpenGlHelper.lastBrightnessY;
		}
		
		glow_stack.add( glow_degree - max_glow );
		
		if ( glow_degree > max_glow )
		{
			max_glow = glow_degree;
			__setLightmapWithGlow( glow_degree );
		}
	}
	
	/**
	 * @see #glowOn(float)
	 */
	public static void glowOff()
	{
		final int idx = glow_stack.size() - 1;
		final float glow_delta = glow_stack.remove( idx );
		if ( glow_stack.isEmpty() )
		{
			OpenGlHelper.setLightmapTextureCoords(
				OpenGlHelper.lightmapTexUnit,
				lightmap_base_x, lightmap_base_y
			);
			GL11.glPopAttrib();  // TODO: Validate if this is needed.
		}
		else if ( glow_delta > 0.0F )
		{
			max_glow -= glow_delta;
			__setLightmapWithGlow( max_glow );
		}
	}
	
	private static void __setLightmapWithGlow( float glow )
	{
		// Append extra brightness for glow.
		final float extra_brightness = glow * 240.0F;
		OpenGlHelper.setLightmapTextureCoords(
			OpenGlHelper.lightmapTexUnit,
			Math.min( 240.0F, lightmap_base_x + extra_brightness ),
			Math.min( 240.0F, lightmap_base_y + extra_brightness )
		);
	}
	
	private GLUtil() { }
}
