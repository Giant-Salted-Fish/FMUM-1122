package com.fmum.mag;

import com.fmum.ammo.IAmmoType;
import com.fmum.item.EquippedWrapper;
import com.fmum.item.IEquippedItem;
import com.fmum.item.IItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

import java.util.OptionalInt;

public class EquippedUnloading extends EquippedWrapper
{
	protected float progress = 0.0F;
	
	public EquippedUnloading( IEquippedItem wrapped ) {
		super( wrapped );
	}
	
	@Override
	public IEquippedItem tickInHand( EnumHand hand, IItem item, EntityPlayer player )
	{
		if ( this.progress >= 1.0F )
		{
			final IMag mag = IMag.from( item );
			if ( mag.isEmpty() ) {
				return this.wrapped;
			}
			
			this.progress = 0.0F;
		}
		
		final MagType type = ( MagType ) item.getType();
		final MagOpConfig config = type.unload_ammo_op;
		final float progress = this.progress + config.progressor;
		final float effect_time = config.effect_time;
		if ( this.progress < effect_time && effect_time <= progress )
		{
			final IMag mag = IMag.from( item );
			if ( mag.isEmpty() ) {
				return this.wrapped;
			}
			
			// Give player at most one if they do not have this type of ammo.
			final IAmmoType ammo = mag.popAmmo();
			if ( player.isCreative() )
			{
				final OptionalInt slot = IAmmoType.lookupValidAmmoSlot( player.inventory, ammo::equals, 0 );
				if ( !slot.isPresent() )
				{
					final ItemStack stack = ammo.newItemStack( ( short ) 0 );
					player.addItemStackToInventory( stack );
				}
			}
			else
			{
				final ItemStack stack = ammo.newItemStack( ( short ) 1 );
				final boolean success = player.addItemStackToInventory( stack );
				if ( !success ) {
					player.dropItem( stack, false );
				}
			}
		}
		
		this.progress = Math.min( 1.0F, progress );
		return this;
	}
}
