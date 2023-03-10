package com.mcwb.client.item;

import com.mcwb.client.render.IAnimator;
import com.mcwb.client.render.IRenderer;
import com.mcwb.common.item.IItem;
import com.mcwb.common.meta.IContexted;

import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IItemRenderer< T extends IContexted > extends IRenderer
{
	public static final String CHANNEL_ITEM = "item";
	
	/**
	 * Called each tick if this item is holden in hand
	 */
	@SideOnly( Side.CLIENT )
	public void tickInHand( T contexted, EnumHand hand );
	
	@SideOnly( Side.CLIENT )
	public IAnimator onTakeOut( T contexted, EnumHand hand );
	
	// TODO: validate if this is needed
//	@SideOnly( Side.CLIENT )
//	public IAnimator onInHandStackChanged
	
	/**
	 * Called before the hand render if it is holden in hand
	 */
	@SideOnly( Side.CLIENT )
	public default void prepareRenderInHand( T contexted, IAnimator animator, EnumHand hand ) { }
	
	/**
	 * @see IItem#renderInHand(EnumHand)
	 * @return {@code true} if should cancel original in hand render
	 */
	@SideOnly( Side.CLIENT )
	public boolean renderInHand( T contexted, EnumHand hand );
	
	/**
	 * @see IItem#onRenderSpecificHand(EnumHand)
	 * @return {@code true} if should cancel original in hand render
	 */
	@SideOnly( Side.CLIENT )
	public boolean onRenderSpecificHand( T contexted, EnumHand hand );
	
	@SideOnly( Side.CLIENT )
	public void render( T contexted );
}
