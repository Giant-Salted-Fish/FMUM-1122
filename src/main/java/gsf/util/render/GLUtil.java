package gsf.util.render;

import gsf.util.math.AxisAngle4f;
import gsf.util.math.Mat4f;
import gsf.util.math.MoreMath;
import gsf.util.math.Quat4f;
import gsf.util.math.Vec3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.util.LinkedList;

@SideOnly( Side.CLIENT )
public final class GLUtil
{
	private static final Minecraft MC = Minecraft.getMinecraft();
	private static final FloatBuffer FLOAT_BUF = BufferUtils.createFloatBuffer( 16 );
	
	public static void bindTexture( ResourceLocation texture ) {
		MC.renderEngine.bindTexture( texture );
	}
	
	/**
	 * Not thread safe.
	 */
	public static void glMultMatrix( Mat4f mat )
	{
		FLOAT_BUF.clear();
		mat.store( FLOAT_BUF );
		FLOAT_BUF.flip();
		GL11.glMultMatrix( FLOAT_BUF );
	}
	
	public static void glTranslateV3f( Vec3f vec ) {
		GL11.glTranslatef( vec.x, vec.y, vec.z );
	}
	
	public static void glRotateAA4f( AxisAngle4f rot ) {
		GL11.glRotatef( MoreMath.toDegrees( rot.angle ), rot.x, rot.y, rot.z );
	}
	
	/**
	 * Not thread safe.
	 */
	public static void glRotateQ4f( Quat4f quat )
	{
		final Mat4f mat = Mat4f.allocate();
		mat.set( quat );
		glMultMatrix( mat );
		Mat4f.release( mat );
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
	
	public static void glEulerRotateYXZ( Vec3f euler_angle )
	{
		GL11.glRotatef( euler_angle.y, 0F, 1F, 0F );
		GL11.glRotatef( euler_angle.x, 1F, 0F, 0F );
		GL11.glRotatef( euler_angle.z, 0F, 0F, 1F );
	}
	
	public static void glScale1f( float factor ) {
		GL11.glScalef( factor, factor, factor );
	}
	
	// >>> Stuff For Glow Effect <<<
	private static float ori_lightmap_x = 0.0F;
	private static float ori_lightmap_y = 0.0F;
	
	private static final LinkedList< Float > glow_stack = new LinkedList<>();
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
			ori_lightmap_x = OpenGlHelper.lastBrightnessX;
			ori_lightmap_y = OpenGlHelper.lastBrightnessY;
		}
		
		glow_stack.push( glow_degree - max_glow );
		
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
		final float glow_delta = glow_stack.pop();
		if ( glow_stack.isEmpty() )
		{
			final float x = ori_lightmap_x;
			final float y = ori_lightmap_y;
			OpenGlHelper.setLightmapTextureCoords( OpenGlHelper.lightmapTexUnit, x, y );
			GL11.glPopAttrib();  // TODO: Validate if this is needed.
			max_glow = 0.0F;
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
		final float x = Math.min( 240.0F, ori_lightmap_x + extra_brightness );
		final float y = Math.min( 240.0F, ori_lightmap_y + extra_brightness );
		OpenGlHelper.setLightmapTextureCoords( OpenGlHelper.lightmapTexUnit, x, y );
	}
	// >>> Glow Stuff End <<<
	
	private GLUtil() { }
}
