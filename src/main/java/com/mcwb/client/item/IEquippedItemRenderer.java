package com.mcwb.client.item;

import com.mcwb.client.render.IAnimator;
import com.mcwb.common.item.IEquippedItem;

import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IEquippedItemRenderer< E > // Animated item renderer
{
	@SideOnly( Side.CLIENT )
	public void tickInHand( E equipped, EnumHand hand );
	
	/**
	 * Called before the hand render if it is holden in hand
	 */
	@SideOnly( Side.CLIENT )
	public void prepareRenderInHandSP( E equipped, EnumHand hand );
	
	/**
	 * @see IEquippedItem#renderInHandSP(EnumHand)
	 * @return {@code true} if should cancel original in hand render
	 */
	@SideOnly( Side.CLIENT )
	public boolean renderInHandSP( E equipped, EnumHand hand );
	
	/**
	 * @see IEquippedItem#onRenderSpecificHandSP(EnumHand)
	 * @return {@code true} if should cancel original in hand render
	 */
	@SideOnly( Side.CLIENT )
	public boolean onRenderSpecificHandSP( E equipped, EnumHand hand );
	
	@SideOnly( Side.CLIENT )
	public IAnimator animator();
}
