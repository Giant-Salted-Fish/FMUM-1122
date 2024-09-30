package com.fmum.gunpart;

import com.fmum.item.EquippedWrapper;
import com.fmum.item.IEquippedItem;
import com.fmum.item.IItem;
import gsf.util.animation.IAnimator;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public abstract class EquippedWrapRenderC extends EquippedWrapper
{
	protected EquippedWrapRenderC( IEquippedItem wrapped ) {
		super( wrapped );
	}
	
	@Override
	public void prepareRenderInHand( EnumHand hand, IItem item )
	{
		final IGunPart delegate = this._getRenderDelegate( hand, item );
		final IAnimator animator = this._getInHandAnimator( hand, item );
		final EquippedGunPart eq = ( EquippedGunPart ) this.wrapped;
		eq.EquippedGunPart$doPrepareRenderInHand( delegate, animator );
	}
	
	protected IGunPart _getRenderDelegate( EnumHand hand, IItem item ) {
		return IGunPart.from( item );
	}
	
	protected IAnimator _getInHandAnimator( EnumHand hand, IItem item )
	{
		final EquippedGunPart eq = ( EquippedGunPart ) this.wrapped;
		return eq.EquippedGunPart$getInHandAnimator( hand, item );
	}
	
	@Override
	public boolean renderInHand( EnumHand hand, IItem item ) {
		return this.wrapped.renderInHand( hand, null );
	}
	
	@Override
	public boolean renderSpecificInHand( EnumHand hand, IItem item ) {
		return this.wrapped.renderSpecificInHand( hand, null );
	}
}
