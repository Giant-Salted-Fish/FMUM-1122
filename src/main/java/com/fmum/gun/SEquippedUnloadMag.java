package com.fmum.gun;

import com.fmum.input.IInput;
import com.fmum.item.EquippedWrapper;
import com.fmum.item.IItem;
import com.fmum.item.IMainEquipped;
import com.fmum.mag.IMag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SEquippedUnloadMag extends EquippedWrapper
{
	protected int tick_left;
	
	public SEquippedUnloadMag( IMainEquipped wrapped, IItem item )
	{
		super( wrapped );
		
		final GunType type = ( GunType ) item.getType();
		this.tick_left = type.op_unload_mag.tick_count;
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
		if ( config.tick_commit + tick_left == config.tick_count )
		{
			final boolean success = this._doUnloadMag( item, player );
			if ( !success ) {
				return this.wrapped;
			}
		}
		
		this.tick_left = tick_left;
		return this;
	}
	
	protected boolean _doUnloadMag( IItem item, EntityPlayer player )
	{
		final IGun gun = IGun.from( item );
		if ( !gun.getMag().isPresent() ) {
			return false;
		}
		
		final IMag mag = gun.popMag();
		final ItemStack stack = mag.takeAndToStack();
		final boolean success = player.addItemStackToInventory( stack );
		if ( !success ) {
			player.dropItem( stack, false );
		}
		return true;
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public IMainEquipped onInputUpdate( String name, IInput input, IItem item ) {
		throw new UnsupportedOperationException();
	}
}
