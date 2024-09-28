package com.fmum.gun;

import com.fmum.animation.SoundFrame;
import com.fmum.gunpart.EquippedGunPart;
import com.fmum.gunpart.IGunPart;
import com.fmum.input.IInput;
import com.fmum.item.EquippedWrapper;
import com.fmum.item.IEquippedItem;
import com.fmum.item.IItem;
import gsf.util.animation.IAnimator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class EquippedInspectClient extends EquippedWrapper
{
	protected int tick_left;
	protected int sound_idx = 0;
	
	public EquippedInspectClient( IEquippedItem wrapped, IItem item )
	{
		super( wrapped );
		
		final GunType type = ( GunType ) item.getType();
		this.tick_left = type.op_inspect.tick_count;
	}
	
	@Override
	public IEquippedItem tickInHand( EnumHand hand, IItem item, EntityPlayer player )
	{
		if ( this.tick_left == 0 ) {
			return this.wrapped;
		}
		
		final GunType type = ( GunType ) item.getType();
		final GunOpConfig config = type.op_inspect;
		final int tick_left = this.tick_left - 1;
		final float progress = 1.0F - ( float ) tick_left / config.tick_count;
		this.sound_idx = SoundFrame.playSound( config.sound_frame, this.sound_idx, progress, player );
		this.tick_left = tick_left;
		return this;
	}
	
	@Override
	public void prepareRenderInHand( EnumHand hand, IItem item )
	{
		final IGunPart self = IGunPart.from( item );
		final IAnimator animator = this._getInHandAnimator( hand, item );
		final EquippedGunPart eq = ( EquippedGunPart ) this.wrapped;
		eq.EquippedGunPart$doPrepareRenderInHand( self , animator );
	}
	
	@Override
	public IEquippedItem onInputUpdate( String name, IInput input, IItem item ) {
		return this;
	}
	
	protected IAnimator _getInHandAnimator( EnumHand hand, IItem item )
	{
		final EquippedGunPart eq = ( EquippedGunPart ) this.wrapped;
		final IAnimator base = eq.EquippedGunPart$getInHandAnimator( hand, item );
		
		final GunType type = ( GunType ) item.getType();
		final GunOpConfig config = type.op_inspect;
		final float progress = _getProgress( this.tick_left, config.tick_count );
		final IAnimator animation = config.animation.ofProgress( progress );
		return IAnimator.compose( animation, base );
	}
}
