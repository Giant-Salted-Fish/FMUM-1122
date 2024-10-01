package com.fmum.gun;

import com.fmum.animation.SoundFrame;
import com.fmum.gunpart.CEquippedWrapRender;
import com.fmum.gunpart.EquippedGunPart;
import com.fmum.input.IInput;
import com.fmum.item.IEquippedItem;
import com.fmum.item.IItem;
import gsf.util.animation.IAnimator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class CEquippedTakeOut extends CEquippedWrapRender
{
	protected int tick_left;
	protected int sound_idx = 0;
	
	public CEquippedTakeOut( IEquippedItem wrapped, IItem item )
	{
		super( wrapped );
		
		final GunType type = ( GunType ) item.getType();
		this.tick_left = type.op_take_out.tick_count;
	}
	
	@Override
	public IEquippedItem tickInHand( IItem item, EnumHand hand, EntityPlayer player )
	{
		if ( this.tick_left == 0 ) {
			return this.wrapped.tickInHand( item, hand, player );
		}
		
		final GunType type = ( GunType ) item.getType();
		final GunOpConfig config = type.op_take_out;
		final int tick_left = this.tick_left - 1;
		final float progress = 1.0F - ( float ) tick_left / config.tick_count;
		this.sound_idx = SoundFrame.playSound( config.sounds, this.sound_idx, progress, player );
		this.tick_left = tick_left;
		return this;
	}
	
	@Override
	public IEquippedItem onInputUpdate( IItem item, String name, IInput input ) {
		return this;
	}
	
	@Override
	protected IAnimator _getInHandAnimator( EnumHand hand, IItem item )
	{
		final EquippedGunPart eq = ( EquippedGunPart ) this.wrapped;
		final IAnimator base = eq.EquippedGunPart$getInHandAnimator( hand, item );
		
		final GunType type = ( GunType ) item.getType();
		final GunOpConfig config = type.op_take_out;
		final float progress = _getAnimProg( this.tick_left, config.tick_count );
		final IAnimator animation = config.animation.ofProgress( progress );
		return IAnimator.compose( animation, base );
	}
}
