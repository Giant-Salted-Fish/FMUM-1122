package com.fmum.mag;

import com.fmum.ammo.IAmmoType;
import com.fmum.input.IInput;
import com.fmum.item.EquippedWrapper;
import com.fmum.item.IEquippedItem;
import com.fmum.item.IItem;
import gsf.util.lang.Result;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;
import java.util.function.IntSupplier;

public class EquippedLoading extends EquippedWrapper
{
	public IEquippedItem next;
	
	protected final int ammo_slot;
	
	protected int tick_left;
	
	
	public EquippedLoading( IEquippedItem wrapped, IItem item, int ammo_slot )
	{
		super( wrapped );
		
		this.ammo_slot = ammo_slot;
		this.next = wrapped;
		
		final MagType type = ( MagType ) item.getType();
		this.tick_left = type.op_load_ammo.tick_count;
	}
	
	@Override
	public IEquippedItem tickInHand( IItem item, EnumHand hand, EntityPlayer player )
	{
		if ( this.tick_left == 0 )
		{
			// Tick next here, otherwise we will have one tick lag with client.
			return this.next.tickInHand( item, hand, player );
		}
		
		final MagType type = ( MagType ) item.getType();
		final MagOpConfig config = type.op_load_ammo;
		final int tick_left = this.tick_left - 1;
		if ( config.tick_commit + tick_left == config.tick_count )
		{
			final boolean success = this._doLoadAmmo( item, player );
			if ( !success ) {
				return this.wrapped;
			}
		}
		
		this.tick_left = tick_left;
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
	
	@Override
	@SideOnly( Side.CLIENT )
	public IEquippedItem onInputUpdate( IItem item, String name, IInput input ) {
		throw new UnsupportedOperationException();
	}
}
