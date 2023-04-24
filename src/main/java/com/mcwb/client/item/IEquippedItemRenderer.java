package com.mcwb.client.item;

import com.mcwb.client.render.IAnimator;
import com.mcwb.common.item.IEquippedItem;
import com.mcwb.util.IAnimation;

import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IEquippedItemRenderer< E >
{
	@SideOnly( Side.CLIENT )
	void tickInHand( E equipped, EnumHand hand );
	
	@SideOnly( Side.CLIENT )
	void updateAnimationForRender( E equipped, EnumHand hand );
	
	/**
	 * Called before the hand render if it is holden in hand.
	 */
	@SideOnly( Side.CLIENT )
	void prepareRenderInHandSP( E equipped, EnumHand hand );
	
	/**
	 * @see IEquippedItem#renderInHandSP(EnumHand)
	 * @return {@code true} if should cancel original in hand render.
	 */
	@SideOnly( Side.CLIENT )
	boolean renderInHandSP( E equipped, EnumHand hand );
	
	/**
	 * @see IEquippedItem#onRenderSpecificHandSP(EnumHand)
	 * @return {@code true} if should cancel original in hand render.
	 */
	@SideOnly( Side.CLIENT )
	boolean onRenderSpecificHandSP( E equipped, EnumHand hand );
	
	@SideOnly( Side.CLIENT )
	void useAnimation( IAnimation animation );
	
	@SideOnly( Side.CLIENT )
	IAnimator animator();
}
