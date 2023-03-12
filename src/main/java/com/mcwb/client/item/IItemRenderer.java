package com.mcwb.client.item;

import com.mcwb.client.render.IAnimator;
import com.mcwb.client.render.IRenderer;
import com.mcwb.common.item.IEquippedItem;

import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IItemRenderer< T /* extends IEquippedItem */ > extends IRenderer
{
	public static final String CHANNEL_ITEM = "item";
	
	/**
	 * Called each tick if this item is holden in hand
	 */
	@SideOnly( Side.CLIENT )
	public void tickInHand( T equipped, EnumHand hand );
	
	@SideOnly( Side.CLIENT )
	public IAnimator onTakeOut( IEquippedItem< ? > prevEquipped, EnumHand hand );
	
	/**
	 * Called before the hand render if it is holden in hand
	 */
	@SideOnly( Side.CLIENT )
	public default void prepareRenderInHand( T prevEquipped, IAnimator animator, EnumHand hand ) { }
	
	/**
	 * @see IEquippedItem#renderInHand()
	 * @return {@code true} if should cancel original in hand render
	 */
	@SideOnly( Side.CLIENT )
	public boolean renderInHand( T equipped, IAnimator animator, EnumHand hand );
	
	/**
	 * @see IEquippedItem#onRenderSpecificHand()
	 * @return {@code true} if should cancel original in hand render
	 */
	@SideOnly( Side.CLIENT )
	public boolean onRenderSpecificHand( T equipped, IAnimator animator, EnumHand hand );
	
//	@SideOnly( Side.CLIENT )
//	public void render( T contexted );
}
