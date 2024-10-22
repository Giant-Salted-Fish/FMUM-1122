package com.fmum.gun;

import com.fmum.FMUM;
import com.fmum.animation.SoundFrame;
import com.fmum.gunpart.CEquippedWrapRender;
import com.fmum.gunpart.EquippedGunPart;
import com.fmum.gunpart.IGunPart;
import com.fmum.input.IInput;
import com.fmum.item.IItem;
import com.fmum.item.IMainEquipped;
import com.fmum.module.IModule;
import com.fmum.network.PacketUnloadMag;
import gsf.util.animation.IAnimator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class CEquippedUnloadMag extends CEquippedWrapRender
{
	protected int tick_left;
	protected int sound_idx = 0;
	
	protected IGun delegate;
	
	public CEquippedUnloadMag( IMainEquipped wrapped, IItem item )
	{
		super( wrapped );
		
		// Copy to avoid side effect.
		final IGun gun = IGun.from( item );  // {gun.getMag().isPresent()} has been checked.
		final NBTTagCompound nbt = gun.getBoundNBT().copy();
		this.delegate = ( IGun ) IModule.takeAndDeserialize( nbt );
		
		final GunType type = ( GunType ) item.getType();
		this.tick_left = type.op_unload_mag.tick_count;
		
		FMUM.NET.sendPacketC2S( new PacketUnloadMag() );
	}
	
	@Override
	public IMainEquipped tickInHand( IItem item, EntityPlayer player )
	{
		if ( this.tick_left == 0 ) {
			return this.wrapped.tickInHand( item, player );
		}
		
		final GunType type = ( GunType ) item.getType();
		final GunOpConfig config = type.op_unload_mag;
		final int tick_left = this.tick_left - 1;
		final float progress = 1.0F - ( float ) tick_left / config.tick_count;
		this.sound_idx = SoundFrame.playSound( config.sounds, this.sound_idx, progress, player );
		this.tick_left = tick_left;
		return this;
	}
	
	@Override
	protected IGunPart _getRenderDelegate( IItem item ) {
		return this.delegate;
	}
	
	@Override
	protected IAnimator _getInHandAnimator( IItem item )
	{
		final EquippedGunPart eq = ( EquippedGunPart ) this.wrapped;
		final IAnimator base = eq.EquippedGunPart$getInHandAnimator( item );
		
		final GunType type = ( GunType ) item.getType();
		final GunOpConfig config = type.op_unload_mag;
		final float progress = _getAnimProg( this.tick_left, config.tick_count );
		final IAnimator animation = config.animation.query( progress );
		return IAnimator.compose( base, animation );
	}
	
	@Override
	public IMainEquipped onInputUpdate( String name, IInput input, IItem item ) {
		return this;
	}
}
