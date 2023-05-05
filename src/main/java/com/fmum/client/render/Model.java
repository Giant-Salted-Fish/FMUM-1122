package com.fmum.client.render;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.fmum.client.FMUMClient;
import com.fmum.common.FMUMResource;
import com.fmum.common.IAutowireLogger;
import com.fmum.common.load.IBuildable;
import com.fmum.common.load.IContentProvider;
import com.fmum.util.AngleAxis4f;
import com.fmum.util.Mat4f;
import com.fmum.util.Mesh;
import com.fmum.util.Vec3f;
import com.google.gson.annotations.SerializedName;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class Model implements IBuildable< Object >, IAutowireLogger
{
	public static final ResourceLocation
		TEXTURE_RED = new FMUMResource( "textures/0xff0000.png" ),
		TEXTURE_GREEN = new FMUMResource( "textures/0x00ff00.png" ),
		TEXTURE_BLUE = new FMUMResource( "textures/0x0000ff.png" );
	
	@SerializedName( value = "mesh" )
	protected String meshPath = ""; // TODO: maybe set to null after load?
	protected transient Mesh mesh;
	
	protected float scale = 1F;
	
	protected boolean tbObjAdapt = false;
	
	public Model() { }
	
	// For convenience.
	public Model( String mesh, float scale, boolean tbObjAdapt )
	{
		this.meshPath = mesh;
		this.scale = scale;
		this.tbObjAdapt = tbObjAdapt;
		this.build( "", FMUMClient.MOD );
	}
	
	@Override
	public Object build( String path, IContentProvider provider )
	{
		provider.regisMeshLoadSubscriber( () -> this.onMeshLoad( provider ) );
		return this;
	}
	
	protected void onMeshLoad( IContentProvider provider ) {
		this.mesh = this.loadMesh( this.meshPath, provider );
	}
	
	public void render() { this.mesh.render(); }
	
	protected Mesh loadMesh( String path, IContentProvider provider )
	{
		return provider.loadMesh(
			path,
			builder -> {
				float scale = this.scale;
				if ( this.tbObjAdapt )
				{
					builder.swapXZ();
					scale *= 16F;
				}
				return scale != 1F ? builder.scale( scale ) : builder;
			}
		);
	}
	
	/// *** For glow stuff. *** ///
	private static float
		lightmapLastX = 0F,
		lightmapLastY = 0F;
	
	private static int glowStack = 0;  // TODO: maybe avoid if?
	
	/**
	 * Call {@link #glowOn(float)} with {@code 1F}.
	 */
	public static void glowOn() { glowOn( 1F ); }
	
	/**
	 * Models rendered after this call will be glowed up. Call {@link #glowOff()} after you complete
	 * light stuff render.
	 * 
	 * @param glowFactor Range from {@code 0F-1F} to control how much to glow.
	 */
	public static void glowOn( float glowFactor )
	{
		final boolean firstPush = glowStack++ == 0;
		if ( firstPush )
		{
			// Push light bits and record previous brightness.
			GL11.glPushAttrib( GL11.GL_LIGHTING_BIT );
			lightmapLastX = OpenGlHelper.lastBrightnessX;
			lightmapLastY = OpenGlHelper.lastBrightnessY;
			
			// Append extra brightness for glow.
			final float extraBrightness = glowFactor * 240F;
			OpenGlHelper.setLightmapTextureCoords(
				OpenGlHelper.lightmapTexUnit,
				Math.min( lightmapLastX + extraBrightness, 240F ),
				Math.min( lightmapLastY + extraBrightness, 240F )
			);
		}
	}
	
	/**
	 * Pair call for {@link #glowOn(float)}.
	 */
	public static void glowOff()
	{
		final boolean lastPop = --glowStack == 0;
		if ( lastPop )
		{
			OpenGlHelper.setLightmapTextureCoords(
				OpenGlHelper.lightmapTexUnit,
				lightmapLastX, lightmapLastY
			);
			GL11.glPopAttrib();
		}
	}
	
	private static final FloatBuffer MAT_BUF = BufferUtils.createFloatBuffer( 16 );
	/**
	 * THIS IS NOT THREAD SAFE!
	 */
	public static void glMulMatrix( Mat4f mat ) // TODO: synchronize?
	{
		MAT_BUF.clear();
		mat.store( MAT_BUF );
		MAT_BUF.flip();
		GL11.glMultMatrix( MAT_BUF );
	}
	
	public static void glTranslatef( Vec3f trans ) {
		GL11.glTranslatef( trans.x, trans.y, trans.z );
	}
	
	public static void glRotatef( AngleAxis4f rot ) {
		GL11.glRotatef( rot.angle, rot.x, rot.y, rot.z );
	}
	
	public static void glEulerRotateYXZ( Vec3f rot )
	{
		GL11.glRotatef( rot.y, 0F, 1F, 0F );
		GL11.glRotatef( rot.x, 1F, 0F, 0F );
		GL11.glRotatef( rot.z, 0F, 0F, 1F );
	}
	
	public static void glScalef( float scale ) { GL11.glScalef( scale, scale, scale ); }
}
