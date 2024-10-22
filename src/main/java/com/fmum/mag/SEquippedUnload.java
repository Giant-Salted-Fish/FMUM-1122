package com.fmum.mag;

import com.fmum.ammo.IAmmoType;
import com.fmum.input.IInput;
import com.fmum.item.EquippedWrapper;
import com.fmum.item.IItem;
import com.fmum.item.IMainEquipped;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.OptionalInt;

public class SEquippedUnload extends EquippedWrapper
{
	protected int tick_left = 0;
	
	public SEquippedUnload( IMainEquipped wrapped ) {
		super( wrapped );
	}
	
	@Override
	public IMainEquipped tickInHand( IItem item, EntityPlayer player )
	{
		if ( this.tick_left == 0 )
		{
			final IMag mag = IMag.from( item );
			if ( mag.isEmpty() ) {
				return this.wrapped.tickInHand( item, player );
			}
			
			final MagType type = ( MagType ) item.getType();
			this.tick_left = type.op_unload_ammo.tick_count;
		}
		
		final MagType type = ( MagType ) item.getType();
		final MagOpConfig config = type.op_unload_ammo;
		final int tick_left = this.tick_left - 1;
		if ( config.tick_commit + tick_left == config.tick_count )
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
		
		this.tick_left = tick_left;
		return this;
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public IMainEquipped onInputUpdate( String name, IInput input, IItem item ) {
		throw new UnsupportedOperationException();
	}
}
