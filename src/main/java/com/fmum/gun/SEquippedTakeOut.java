package com.fmum.gun;

import com.fmum.input.IInput;
import com.fmum.item.EquippedWrapper;
import com.fmum.item.IItem;
import com.fmum.item.IMainEquipped;
import net.minecraft.entity.player.EntityPlayer;

public class SEquippedTakeOut extends EquippedWrapper
{
	protected int tick_left;
	
	public SEquippedTakeOut( IMainEquipped wrapped, IItem item )
	{
		super( wrapped );
		
		final GunType type = ( GunType ) item.getType();
		this.tick_left = type.op_take_out.tick_count - 1;  // Catch up client.
		assert this.tick_left >= 0;
	}
	
	@Override
	public IMainEquipped tickInHand( IItem item, EntityPlayer player )
	{
		if ( this.tick_left == 0 ) {
			return this.wrapped.tickInHand( item, player );
		}
		
//		final GunType type = ( GunType ) item.getType();
//		final GunOpConfig config = type.op_take_out;
//		final int tick_left = this.tick_left - 1;
//		this.tick_left = tick_left;
		this.tick_left -= 1;
		return this;
	}
	
	@Override
	public IMainEquipped onInputUpdate( String name, IInput input, IItem item ) {
		throw new UnsupportedOperationException();
	}
}
