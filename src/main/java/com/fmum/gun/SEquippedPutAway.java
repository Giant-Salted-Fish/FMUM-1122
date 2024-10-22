package com.fmum.gun;

import com.fmum.input.IInput;
import com.fmum.item.EquippedWrapper;
import com.fmum.item.IItem;
import com.fmum.item.IMainEquipped;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Optional;

public class SEquippedPutAway extends EquippedWrapper
{
	protected int tick_left;
	
	public SEquippedPutAway( IMainEquipped wrapped, IItem item )
	{
		super( wrapped );
		
		final GunType type = ( GunType ) item.getType();
		this.tick_left = type.op_put_away.tick_count - 1;  // Catch up client.
		assert this.tick_left >= 0;
	}
	
	@Override
	public Optional< IMainEquipped > tickPutAway( IItem item, EntityPlayer player )
	{
		if ( this.tick_left == 0 ) {
			return Optional.empty();
		}

//		final GunType type = ( GunType ) item.getType();
//		final GunOpConfig config = type.op_take_out;
//		final int tick_left = this.tick_left - 1;
//		this.tick_left = tick_left;
		this.tick_left -= 1;
		return Optional.of( this );
	}
	
	@Override
	public IMainEquipped tickInHand( IItem item, EntityPlayer player ) {
		return this.wrapped.tickInHand( item, player );
	}
	
	@Override
	public IMainEquipped onInputUpdate( String name, IInput input, IItem item )
	{
		return null;
	}
}
