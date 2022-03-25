package com.fmum.client.model;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import com.fmum.client.FMUMClient;
import com.fmum.client.ResourceManager;
import com.fmum.common.FMUM;
import com.fmum.common.type.TypeInfo;
import com.fmum.common.util.CoordSystem;
import com.fmum.common.util.Vec3f;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

// TODO: may override render methods in super class?
public abstract class Model extends ModelBase
{
	/**
	 * A refer to Minecraft instance for convenience
	 */
	protected static final Minecraft mc = FMUM.mc;
	
	/**
	 * Buffered instances for convenient operations
	 */
	protected static final CoordSystem sys = new CoordSystem();
	protected static final Vec3f vec = new Vec3f();
	
	/**
	 * Partial tick time. This could be used widely hence it is set as a static variable to avoid
	 * passing this value through so much layers. Its value should be setup before calling any
	 * render method in {@link Model} class.
	 */
	public static float smoother = 0F;
	
	public void renderFP(ItemStack stack, TypeInfo type)
	{
		// Re-setup projection matrix
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		Project.gluPerspective(
			getFOVModifier(smoother),
			(float)mc.displayWidth / mc.displayHeight,
			0.05F,
			mc.gameSettings.renderDistanceChunks * 16 * MathHelper.SQRT_2
		);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		
		// Switch y and z axis
		GL11.glRotatef(90F, 0F, 1F, 0F);
		
		/** for test */
		float[] f = FMUMClient.testList.get(0).testFloat;
		GL11.glTranslatef(f[0], f[1], f[2]);
		GL11.glRotatef(f[4], 0F, 1F, 0F);
		GL11.glRotatef(f[5], 0F, 0F, 1F);
		GL11.glRotatef(f[3], 1F, 0F, 0F);
		/** for test */
		
		this.doRenderFP(stack, type);
	}
	
	/**
	 * Preliminary render method without any additional render information provided, hence it can be
	 * called in any context. It is recommended to simply render your model in this method to ensure
	 * the compatibility.
	 */
	public void render() { }
	
	@Override
	public void render(
		Entity entityIn,
		float limbSwing,
		float limbSwingAmount,
		float ageInTicks,
		float netHeadYaw,
		float headPitch,
		float scale
	) { this.render(); }
	
	/**
	 * Called in each tick. In default it updates first person animator.
	 */
	public void tick() { this.getAnimatorFP().tick(this); }
	
	/**
	 * Only one first person animator required at a time. Hence it can be bind to the model.
	 */
	public Animator getAnimatorFP() { return null; } // TODO: default instance
	
	/**
	 * Render item holding first person. Projection is setup and coordinate system is oriented at
	 * camera location.
	 * 
	 * @param stack Corresponding stack of the model
	 * @param type Type
	 */
	protected void doRenderFP(ItemStack stack, TypeInfo type) { this.render(); }
	
	protected static void bindTexture(String textureLocation) {
		mc.renderEngine.bindTexture(ResourceManager.getTexture(textureLocation));
	}
	
	/**
	 * Based {@link net.minecraft.client.renderer.EntityRenderer#getFOVModifier(float, boolean)}
	 */
	protected static float getFOVModifier(float smoother)
	{
		float fov = FMUMClient.settings.fovSetting;
		return(
			ActiveRenderInfo.getBlockStateAtEntityViewpoint(
				mc.world,
				mc.getRenderViewEntity(),
				smoother
			).getMaterial() == Material.WATER
			? fov * 6F / 7F
			: fov
		);
	}
	
	/**
	 * For lighting stuff
	 */
	private static float
		lightmapLastX = 0F,
		lightmapLastY = 0F;
	
	protected static void glowOn(int glow)
	{
		GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
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
	
	protected static void glowOn()
	{
		GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
		lightmapLastX = OpenGlHelper.lastBrightnessX;
		lightmapLastY = OpenGlHelper.lastBrightnessY;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
	}
	
	protected static void glowOff()
	{
		OpenGlHelper.setLightmapTextureCoords(
			OpenGlHelper.lightmapTexUnit,
			lightmapLastX,
			lightmapLastY
		);
		GL11.glPopAttrib();
	}
}
