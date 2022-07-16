package com.fmum.client.item;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import com.fmum.client.FMUMClient;
import com.fmum.client.render.Renderable;
import com.fmum.common.item.MetaItem;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MouseHelper;
import net.minecraft.util.math.MathHelper;

public interface RenderableItem extends Renderable
{
	public default void onRenderTick( ItemStack stack, MetaItem meta, MouseHelper mouse )
	{
		
	}
	
	/**
	 * Render this model in first person view. Note that x and z axis are swapped in default.
	 */
	public default void renderInHand( ItemStack stack, MetaItem meta )
	{
		// Re-setup projection matrix
		GL11.glMatrixMode( GL11.GL_PROJECTION );
		GL11.glLoadIdentity();
		Project.gluPerspective(
			this.fovModifier( this.smoother() ),
			( float ) FMUMClient.mc.displayWidth / FMUMClient.mc.displayHeight,
			0.05F, // TODO: maybe smaller this value to avoid seeing through the parts
			FMUMClient.settings.renderDistanceChunks * 16 * MathHelper.SQRT_2
		);
		GL11.glMatrixMode( GL11.GL_MODELVIEW );
		
		// Swap y and z axis
		GL11.glRotatef( 90F, 0F, 1F, 0F );
		
		/** for test 
		double[] d = FMUMClient.testList.get( 0 ).testValue;
		GL11.glTranslated( d[ 0 ], d[ 1 ], d[ 2 ] );
		GL11.glRotated( d[ 4 ], 0D, 1D, 0D );
		GL11.glRotated( d[ 5 ], 0D, 0D, 1D );
		GL11.glRotated( d[ 3 ], 1D, 0D, 0D );
		/** for test */
		
		this.doRenderInHand( stack, meta );
	}
	
	/**
	 * Preliminary render method without any additional render information provided, hence it can be
	 * called in any context. It is recommended to simply render your model in this method to ensure
	 * the compatibility.
	 */
	public default void doRenderInHand( ItemStack stack, MetaItem meta ) { this.render(); }
	
	/**
	 * Copied from {@link EntityRenderer#getFOVModifier(float, boolean)}
	 */
	public default float fovModifier( float smoother )
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
}
