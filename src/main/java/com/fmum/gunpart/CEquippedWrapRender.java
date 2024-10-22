package com.fmum.gunpart;

import com.fmum.item.EquippedWrapper;
import com.fmum.item.IItem;
import com.fmum.item.IMainEquipped;
import gsf.util.animation.IAnimator;
import gsf.util.math.MoreMath;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public abstract class CEquippedWrapRender extends EquippedWrapper
{
	protected CEquippedWrapRender( IMainEquipped wrapped ) {
		super( wrapped );
	}
	
	@Override
	public void prepareRenderInHand( IItem item )
	{
		final IGunPart delegate = this._getRenderDelegate( item );
		final IAnimator animator = this._getInHandAnimator( item );
		final EquippedGunPart eq = ( EquippedGunPart ) this.wrapped;
		eq.EquippedGunPart$doPrepareRenderInHand( delegate, animator );
	}
	
	protected IGunPart _getRenderDelegate( IItem item ) {
		return IGunPart.from( item );
	}
	
	protected abstract IAnimator _getInHandAnimator( IItem item );
	
	@Override
	public boolean renderInHand( IItem item ) {
		return this.wrapped.renderInHand( null );
	}
	
	@Override
	public boolean renderSpecificInHand( IItem item ) {
		return this.wrapped.renderSpecificInHand( null );
	}
	
	
	protected static float _getAnimProg( int tick_left, int tick_count )
	{
		final float alpha = Minecraft.getMinecraft().getRenderPartialTicks();
		final float partial_tick = MoreMath.lerp( tick_left + 1, tick_left, alpha );
		return 1.0F - partial_tick / tick_count;
	}
}
