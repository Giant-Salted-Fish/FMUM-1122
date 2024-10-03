package com.fmum.gun;

import com.fmum.input.IInput;
import com.fmum.item.EquippedWrapper;
import com.fmum.item.IEquippedItem;
import com.fmum.item.IItem;
import com.fmum.mag.IMag;
import com.fmum.module.IModule;
import gsf.util.lang.Result;
import gsf.util.lang.Type;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;

public class SEquippedLoadMag extends EquippedWrapper
{
	protected final int mag_slot;
	protected int tick_left;
	
	public SEquippedLoadMag( IEquippedItem wrapped, IItem item, int mag_slot )
	{
		super( wrapped );
		
		this.mag_slot = mag_slot;
		
		final GunType type = ( GunType ) item.getType();
		this.tick_left = type.op_load_mag.tick_count;
	}
	
	@Override
	public IEquippedItem tickInHand( IItem item, EnumHand hand, EntityPlayer player )
	{
		if ( this.tick_left == 0 ) {
			return this.wrapped.tickInHand( item, hand, player );
		}
		
		final GunType type = ( GunType ) item.getType();
		final GunOpConfig config = type.op_load_mag;
		final int tick_left = this.tick_left - 1;
		if ( config.tick_commit + tick_left == config.tick_count )
		{
			final boolean success = this._doLoadMag( item, player );
			if ( !success ) {
				return this.wrapped;
			}
		}
		
		this.tick_left = tick_left;
		return this;
	}
	
	protected boolean _doLoadMag( IItem item, EntityPlayer player )
	{
		final IGun gun = IGun.from( item );
		if ( gun.getMag().isPresent() ) {
			return false;
		}
		
		final ItemStack stack = player.inventory.getStackInSlot( this.mag_slot );
		final Optional< IMag > mag = (
			IItem.ofOrEmpty( stack )
			.flatMap( it -> it.lookupCapability( IModule.CAPABILITY ) )
			.flatMap( mod -> Type.cast( mod, IMag.class ) )
		);
		if ( !mag.isPresent() ) {
			return false;
		}
		
		final Result< Runnable, String > result = gun.checkMagForLoad( mag.get() );
		if ( !result.isSuccess() ) {
			return false;
		}
		
		result.unwrap().run();
		stack.shrink( 1 );
		return true;
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public IEquippedItem onInputUpdate( IItem item, String name, IInput input ) {
		throw new UnsupportedOperationException();
	}
}
