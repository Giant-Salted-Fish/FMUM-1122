package com.fmum.gun;

import com.fmum.animation.SoundFrame;
import com.fmum.gunpart.CEquippedWrapRender;
import com.fmum.gunpart.EquippedGunPart;
import com.fmum.input.IInput;
import com.fmum.item.IItem;
import com.fmum.item.IMainEquipped;
import gsf.util.animation.IAnimator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;

@SideOnly( Side.CLIENT )
public class CEquippedPutAway extends CEquippedWrapRender
{
	protected int tick_left;
	protected int sound_idx = 0;
	
	public CEquippedPutAway( IMainEquipped wrapped, IItem item )
	{
		super( wrapped );
		
		final GunType type = ( GunType ) item.getType();
		this.tick_left = type.op_put_away.tick_count;
	}
	
	@Override
	public Optional< IMainEquipped > tickPutAway( IItem item, EntityPlayer player )
	{
		if ( this.tick_left == 0 ) {
			return Optional.empty();
		}
		
		final GunType type = ( GunType ) item.getType();
		final GunOpConfig config = type.op_put_away;
		final int tick_left = this.tick_left - 1;
		final float progress = 1.0F - ( float ) tick_left / config.tick_count;
		this.sound_idx = SoundFrame.playSound( config.sounds, this.sound_idx, progress, player );
		this.tick_left = tick_left;
		return Optional.of( this );
	}
	
	@Override
	public IMainEquipped tickInHand( IItem item, EntityPlayer player ) {
		return this.wrapped.tickInHand( item, player );
	}
	
	@Override
	protected IAnimator _getInHandAnimator( IItem item )
	{
		final EquippedGunPart eq = ( EquippedGunPart ) this.wrapped;
		final IAnimator base = eq.EquippedGunPart$getInHandAnimator( item );
		
		final GunType type = ( GunType ) item.getType();
		final GunOpConfig config = type.op_put_away;
		final float progress = _getAnimProg( this.tick_left, config.tick_count );
		final IAnimator animation = config.animation.query( progress );
		return IAnimator.compose( base, animation );
	}
	
	@Override
	public boolean onMouseWheelInput( int dwheel, IItem item ) {
		return false;
	}
	
	@Override
	public IMainEquipped onInputUpdate( String name, IInput input, IItem item ) {
		return this;
	}
}
