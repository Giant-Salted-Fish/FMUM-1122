package com.mcwb.client.render;

import org.lwjgl.opengl.GL11;

import com.google.gson.annotations.SerializedName;
import com.mcwb.client.IAutowireBindTexture;
import com.mcwb.client.MCWBClient;
import com.mcwb.common.IAutowireLogger;
import com.mcwb.common.MCWBResource;
import com.mcwb.common.load.IBuildable;
import com.mcwb.common.load.IRequireMeshLoad;
import com.mcwb.common.pack.IContentProvider;
import com.mcwb.util.Mesh;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class Renderer implements IRenderer, IBuildable< IRenderer >,
	IRequireMeshLoad, IAutowireLogger, IAutowireBindTexture
{
	public static final ResourceLocation
		RED_TEXTURE = new MCWBResource( "textures/0xff0000.png" ),
		GREEN_TEXTURE = new MCWBResource( "textures/0x00ff00.png" ),
		BLUE_TEXTURE = new MCWBResource( "textures/0x0000ff.png" );
	
	/**
	 * Default mesh path initializer
	 */
	protected static final String[] MESH_PATHS = { };
	
	@SerializedName( value = "meshPaths", alternate = "meshes" )
	protected String[] meshPaths = MESH_PATHS;
	
	protected float scale = 1F;
	
	protected boolean tbObjAdapt = false;
	
	protected transient Mesh[] meshes;
	
	@Override
	public IRenderer build( String path, IContentProvider provider )
	{
		provider.regisMeshLoad( this );
		return this;
	}
	
	@Override
	public void onMeshLoad()
	{
		this.meshes = new Mesh[ this.meshPaths.length ];
		for(
			int i = this.meshPaths.length;
			i-- > 0;
			this.meshes[ i ] = MCWBClient.MOD.loadMesh(
				this.meshPaths[ i ],
				builder -> {
					float scale = this.scale;
					if( this.tbObjAdapt )
					{
						builder.swapXZ();
						scale *= 16F;
					}
					return scale != 1F ? builder.scale( scale ) : builder;
				}
			)
		);
	}
	
	@Override
	public void render() { for( Mesh mesh : this.meshes ) mesh.render(); }
	
	/// For glow stuff ///
	private static float
		lightmapLastX = 0F,
		lightmapLastY = 0F;
	
	/**
	 * Call {@link #glowOn(float)} with {@code 1F}
	 */
	protected final void glowOn() { this.glowOn( 1F ); }
	
	/**
	 * Models rendered after this call will be glowed up. Call {@link #glowOff()} after you complete
	 * light stuff render.
	 * 
	 * @param glowFactor Range from {@code 0F-1F} to control how much to glow
	 */
	protected final void glowOn( float glowFactor )
	{
		// Push light bits and record previous brightness
		GL11.glPushAttrib( GL11.GL_LIGHTING_BIT );
		lightmapLastX = OpenGlHelper.lastBrightnessX;
		lightmapLastY = OpenGlHelper.lastBrightnessY;
		
		// Append extra brightness for glow
		final float extraBrightness = glowFactor * 240F;
		OpenGlHelper.setLightmapTextureCoords(
			OpenGlHelper.lightmapTexUnit,
			Math.min( lightmapLastX + extraBrightness, 240F ),
			Math.min( lightmapLastY + extraBrightness, 240F )
		);
	}
	
	/**
	 * Pair call for {@link #glowOn(float)}
	 */
	protected final void glowOff()
	{
		OpenGlHelper.setLightmapTextureCoords(
			OpenGlHelper.lightmapTexUnit,
			lightmapLastX, lightmapLastY
		);
		GL11.glPopAttrib();
	}
}
