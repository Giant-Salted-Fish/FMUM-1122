package com.fmum.client.model;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import com.fmum.client.FMUMClient;
import com.fmum.client.Operation;
import com.fmum.common.type.TypeInfo;
import com.fmum.common.util.CoordSystem;
import com.fmum.common.util.Vec3;

import net.minecraft.block.material.Material;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MouseHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

// TODO: may override render methods in super class?
public abstract class Model extends ModelBase
{
	/**
	 * Buffered instances for convenient operations
	 */
	protected static final CoordSystem sys = CoordSystem.get();
	protected static final Vec3 vec = Vec3.get();
	
	/**
	 * Partial tick time. This could be used widely hence it is set as a static variable to avoid
	 * passing this value through so much layers. Its value should be setup before calling any
	 * render method in {@link Model} class.
	 */
	public static float smoother = 0F;
	
	/**
	 * Render this model in first person view. Note that x and z axis are swapped in default.
	 */
	public void renderFP(ItemStack stack, TypeInfo type)
	{
		// Re-setup projection matrix
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		Project.gluPerspective(
			getFOVModifier(smoother),
			(float)FMUMClient.mc.displayWidth / FMUMClient.mc.displayHeight,
			0.05F,
			FMUMClient.settings.renderDistanceChunks * 16 * MathHelper.SQRT_2
		);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		
		// Switch y and z axis
		GL11.glRotatef(90F, 0F, 1F, 0F);
		
		/** for test */
//		double[] f = FMUMClient.testList.get(0).testFloat;
//		GL11.glTranslated(f[0], f[1], f[2]);
//		GL11.glRotated(f[4], 0D, 1D, 0D);
//		GL11.glRotated(f[5], 0D, 0D, 1D);
//		GL11.glRotated(f[3], 1D, 0D, 0D);
		/** for test */
		
		this.doRenderFP(stack, type);
	}
	
	/**
	 * Preliminary render method without any additional render information provided, hence it can be
	 * called in any context. It is recommended to simply render your model in this method to ensure
	 * the compatibility.
	 */
	public void render() { }
	
	/**
	 * <p>Call in when the corresponding model is focused or high lighted. In default it glows the
	 * model before rendering it.</p>
	 * 
	 * <p>Notice that calling glow twice could cause weird lighting problem. Hence it is recommended
	 * to override this method if you used {@link #glowOn()} or {@link #glowOn(int)} in
	 * {@link #render()}.</p>
	 */
	public void renderHighLighted()
	{
		glowOn();
		this.render();
		glowOff();
	}
	
	/**
	 * Bind texture, scale and render!
	 */
	public void renderItem(ItemStack stack, TypeInfo type)
	{
		bindTexture(type.getTexture(stack));
		final double scale = type.modelScale;
		GL11.glScaled(scale, scale, scale);
		this.render();
	}
	
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
	 * Called in each tick. In default ticks its first person animator.
	 */
	public void itemTick(ItemStack stack, TypeInfo type) {
		this.getAnimatorFP().itemTick(stack, type);
	}
	
	/**
	 * Called in each render tick. In default it updates player's render position and first person
	 * animator.
	 */
	public void itemRenderTick(ItemStack stack, TypeInfo type, MouseHelper mouse) {
		this.getAnimatorFP().itemRenderTick(stack, type, mouse);
	}
	
	/**
	 * Only one first person animator required at a time. Hence it can be bind to the model.
	 */
	public Animator getAnimatorFP() { return AnimatorCamControl.INSTANCE; }
	
	/**
	 * Render item holding first person. Projection is setup and coordinate system is oriented at
	 * camera location.
	 * 
	 * @param stack Corresponding stack of the model
	 * @param type Type
	 */
	protected void doRenderFP(ItemStack stack, TypeInfo type) { this.render(); }
	
	protected static Operation getOperating() { return FMUMClient.operating; }
	
	protected static void bindTexture(ResourceLocation texture) {
		FMUMClient.mc.renderEngine.bindTexture(texture);
	}
	
	/**
	 * Based {@link net.minecraft.client.renderer.EntityRenderer#getFOVModifier(float, boolean)}
	 */
	protected static float getFOVModifier(float smoother)
	{
		float fov = FMUMClient.settings.fovSetting;
		return(
			ActiveRenderInfo.getBlockStateAtEntityViewpoint(
				FMUMClient.mc.world,
				FMUMClient.mc.getRenderViewEntity(),
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
