package com.fmum.gun;

import com.fmum.input.IInput;
import com.fmum.item.EquippedWrapper;
import com.fmum.item.IEquippedItem;
import com.fmum.item.IItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;

import java.util.Optional;

public class SEquippedPutAway extends EquippedWrapper
{
	protected int tick_left;
	
	public SEquippedPutAway( IEquippedItem wrapped, IItem item )
	{
		super( wrapped );
		
		final GunType type = ( GunType ) item.getType();
		this.tick_left = type.op_put_away.tick_count - 1;  // Catch up client.
		assert this.tick_left >= 0;
	}
	
	@Override
	public Optional< IEquippedItem > tickPutAway( IItem item, EnumHand hand, EntityPlayer player )
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
	public IEquippedItem tickInHand( IItem item, EnumHand hand, EntityPlayer player ) {
		return this.wrapped.tickInHand( item, hand, player );
	}
	
	@Override
	public IEquippedItem onInputUpdate( IItem item, String name, IInput input )
	{
		return null;
	}
}
