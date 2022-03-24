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
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

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
		
		/** for test */
		float[] f = FMUMClient.testList.get(0).testFloat;
		GL11.glTranslatef(f[0], f[1], f[2]);
		GL11.glRotatef(f[4], 0F, 1F, 0F);
		GL11.glRotatef(f[5], 0F, 0F, 1F);
		GL11.glRotatef(f[3], 1F, 0F, 0F);
		/** for test */
		
		this.render();
	}
	
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
}
