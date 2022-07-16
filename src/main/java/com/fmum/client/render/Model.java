package com.fmum.client.render;

import org.lwjgl.opengl.GL11;

import com.fmum.common.util.Mesh;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.OpenGlHelper;

public abstract class Model extends ModelBase implements Renderable
{
	/**
	 * A fixed instance that can be used as the initializer value
	 */
	public static final Mesh[] DEF_MESHES = { };
	
	public Mesh[] meshes = DEF_MESHES;
	
	public Model() { }
	
	public Model( Mesh... meshes ) { this.meshes = meshes; }
	
//	public Model( Consumer< Model > initializer ) { initializer.accept( this ); }
	
	@Override
	public void render() { for( Mesh m : this.meshes ) m.render(); }
	
	/// For lighting stuff ///
	private static float
		lightmapLastX = 0F,
		lightmapLastY = 0F;
	
	@Override
	public void glowOn(int glow)
	{
		GL11.glPushAttrib( GL11.GL_LIGHTING_BIT );
		OpenGlHelper.setLightmapTextureCoords(
			OpenGlHelper.lightmapTexUnit,
			Math.min(
				glow / 15F * 240F + (
					lightmapLastX = OpenGlHelper.lastBrightnessX
				),
				240F
			), 
			Math.min(
				glow / 15F * 240F + (
					lightmapLastY = OpenGlHelper.lastBrightnessY
				),
				240F
			)
		);
	}
	
	@Override
	public void glowOn()
	{
		GL11.glPushAttrib( GL11.GL_LIGHTING_BIT );
		lightmapLastX = OpenGlHelper.lastBrightnessX;
		lightmapLastY = OpenGlHelper.lastBrightnessY;
		OpenGlHelper.setLightmapTextureCoords( OpenGlHelper.lightmapTexUnit, 240F, 240F );
	}
	
	@Override
	public void glowOff()
	{
		OpenGlHelper.setLightmapTextureCoords(
			OpenGlHelper.lightmapTexUnit,
			lightmapLastX,
			lightmapLastY
		);
		GL11.glPopAttrib();
	}
}
