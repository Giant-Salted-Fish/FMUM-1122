package com.mcwb.client.item;

import com.mcwb.client.render.IRenderer;
import com.mcwb.common.meta.IContexted;

import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

//@SideOnly( Side.CLIENT )
public interface IItemRenderer< T extends IContexted > extends IRenderer
{
	/**
	 * Called each tick if this item is holden in hand
	 */
	@SideOnly( Side.CLIENT )
	public void onLogicTick( T contexted, EnumHand hand );
	
	/**
	 * Called before the rendering in each frame if this item is holden in hand
	 */
	@SideOnly( Side.CLIENT )
	public void onRenderTick( T contexted, EnumHand hand );
	
	/**
	 * @return {@code true} if should cancel original render
	 */
	@SideOnly( Side.CLIENT )
	public boolean onHandRender( T contexted, EnumHand hand );
	
	/**
	 * @return {@code true} if should cancel original render
	 */
	@SideOnly( Side.CLIENT )
	public boolean onSpecificHandRender( T contexted, EnumHand hand );
}
