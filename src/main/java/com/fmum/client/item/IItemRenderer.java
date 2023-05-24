package com.fmum.client.item;

import com.fmum.client.render.IAnimator;

import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IItemRenderer< C, ER >
{
	@SideOnly( Side.CLIENT )
	ER onTakeOut( EnumHand hand );
	
	/**
	 * Render an item that is not equipped but animated by the given animator
	 */
	@SideOnly( Side.CLIENT )
	void render( C item, IAnimator animator );
}
