package com.fmum.mag;

import com.fmum.ammo.IAmmoType;
import com.fmum.item.EquippedWrapper;
import com.fmum.item.IEquippedItem;
import com.fmum.item.IItem;
import gsf.util.lang.Result;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

import java.util.Optional;
import java.util.function.IntSupplier;

public class EquippedLoading extends EquippedWrapper
{
	public IEquippedItem next;
	
	protected final int ammo_slot;
	
	protected float progress = 0.0F;
	
	
	public EquippedLoading( IEquippedItem wrapped, int ammo_slot )
	{
		super( wrapped );
		
		this.ammo_slot = ammo_slot;
		this.next = wrapped;
	}
	
	@Override
	public IEquippedItem tickInHand( EnumHand hand, IItem item, EntityPlayer player )
	{
		if ( this.progress >= 1.0F ) {
			return this.next;
		}
		
		final MagType type = ( MagType ) item.getType();
		final MagOpConfig config = type.load_ammo_op;
		final float progress = this.progress + config.progressor;
		final float effect_time = config.effect_time;
		if ( this.progress < effect_time && effect_time <= progress )
		{
			final boolean success = this._doLoadAmmo( item, player );
			if ( !success ) {
				return this.wrapped;
			}
		}
		
		this.progress = Math.min( 1.0F, progress );
		return this;
	}
	
	protected boolean _doLoadAmmo( IItem held_item, EntityPlayer player )
	{
		final IMag mag = IMag.from( held_item );
		if ( mag.isFull() ) {
			return false;
		}
		
		final ItemStack stack = player.inventory.getStackInSlot( this.ammo_slot );
		final Optional< IAmmoType > ammo = (
			IItem.ofOrEmpty( stack )
			.map( IItem::getType )
			.filter( IAmmoType.class::isInstance )
			.map( IAmmoType.class::cast )
		);
		if ( !ammo.isPresent() ) {
			return false;
		}
		
		final IAmmoType ammo_type = ammo.get();
		final Result< IntSupplier, String > result = mag.checkAmmoForLoad( ammo_type );
		if ( !result.isSuccess() ) {
			return false;
		}
		
		result.unwrap().getAsInt();
		if ( !player.isCreative() ) {
			stack.shrink( 1 );
		}
		return true;
	}
}
