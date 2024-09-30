package com.fmum.gun;

import com.fmum.animation.SoundFrame;
import com.fmum.gunpart.EquippedGunPart;
import com.fmum.gunpart.EquippedWrapRenderC;
import com.fmum.gunpart.IGunPart;
import com.fmum.input.IInput;
import com.fmum.item.IEquippedItem;
import com.fmum.item.IItem;
import com.fmum.mag.IMag;
import com.fmum.module.IModule;
import gsf.util.animation.IAnimator;
import gsf.util.lang.Result;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.OptionalInt;
import java.util.stream.IntStream;

@SideOnly( Side.CLIENT )
public class EquippedLoadingMag extends EquippedWrapRenderC
{
	protected int tick_left;
	protected int sound_idx = 0;
	
	protected IGun delegate;
	
	public EquippedLoadingMag( IEquippedItem wrapped, IItem item )
	{
		super( wrapped );
		
		final IGun gun = IGun.from( item );
		if ( gun.getMag().isPresent() )
		{
			this.tick_left = 0;
			return;
		}
		
		final EntityPlayerSP player = Minecraft.getMinecraft().player;
		final IntStream slots = IItem.lookupIn( player.inventory, it -> (
			it.lookupCapability( IModule.CAPABILITY )
			.filter( IMag.class::isInstance )
			.map( IMag.class::cast )
			.map( gun::checkMagForLoad )
			.map( Result::isSuccess )
			.orElse( false )
		) );
		final OptionalInt mag_slot = slots.findFirst();
		if ( !mag_slot.isPresent() )
		{
			this.tick_left = 0;
			return;
		}
		
		final ItemStack stack = player.inventory.getStackInSlot( mag_slot.getAsInt() );
		final IMag mag = IMag.from( IItem.ofOrEmpty( stack ).orElseThrow( IllegalStateException::new ) );
		final IMag proxy = mag.IMag$createLoadingProxy();
		
		// Copy to avoid side effect.
		final NBTTagCompound nbt = gun.getBoundNBT().copy();
		final IGun copied = ( IGun ) IModule.takeAndDeserialize( nbt );
		copied.tryInstall( 0, proxy ).apply();
		
		this.delegate = copied;
		
		final GunType type = ( GunType ) item.getType();
		this.tick_left = type.op_load_mag.tick_count;
	}
	
	@Override
	public IEquippedItem tickInHand( EnumHand hand, IItem item, EntityPlayer player )
	{
		if ( this.tick_left == 0 ) {
			return this.wrapped.tickInHand( hand, item, player );
		}
		
		final GunType type = ( GunType ) item.getType();
		final GunOpConfig config = type.op_load_mag;
		final int tick_left = this.tick_left - 1;
		final float progress = 1.0F - ( float ) tick_left / config.tick_count;
		this.sound_idx = SoundFrame.playSound( config.sounds, this.sound_idx, progress, player );
		this.tick_left = tick_left;
		return this;
	}
	
	@Override
	protected IGunPart _getRenderDelegate( EnumHand hand, IItem item ) {
		return this.delegate;
	}
	
	@Override
	protected IAnimator _getInHandAnimator( EnumHand hand, IItem item )
	{
		final EquippedGunPart eq = ( EquippedGunPart ) this.wrapped;
		final IAnimator base = eq.EquippedGunPart$getInHandAnimator( hand, item );
		
		final GunType type = ( GunType ) item.getType();
		final GunOpConfig config = type.op_load_mag;
		final float progress = IEquippedItem.getProgress( this.tick_left, config.tick_count );
		final IAnimator animation = config.animation.ofProgress( progress );
		return IAnimator.compose( animation, base );
	}
	
	@Override
	public IEquippedItem onInputUpdate( String name, IInput input, IItem item ) {
		return this;
	}
}
