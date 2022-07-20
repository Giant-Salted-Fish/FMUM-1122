package com.fmum.client.item;

import com.fmum.client.FMUMClient;
import com.fmum.client.render.RenderableBase;
import com.fmum.common.item.MetaItem;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MouseHelper;

public interface RenderableItem extends RenderableBase
{
	public default void onRenderTick( ItemStack stack, MetaItem meta, MouseHelper mouse )
	{
		// TODO: look around control
	}
	
	/**
	 * Render this model in first person view
	 */
	public default void renderInHand( ItemStack stack, MetaItem meta ) { this.render( meta ); }
	
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
