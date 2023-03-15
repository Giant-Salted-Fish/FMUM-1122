package com.mcwb.client.item;

import com.mcwb.client.render.IAnimator;

import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IItemRenderer< C, ER >
{
	@SideOnly( Side.CLIENT )
	public ER onTakeOut( EnumHand hand );
	
	/**
	 * Render an item that is not equipped but animated by the given animator
	 */
	@SideOnly( Side.CLIENT )
	public void render( C contexted, IAnimator animator );
}
