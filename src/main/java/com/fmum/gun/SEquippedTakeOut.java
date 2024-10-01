package com.fmum.gun;

import com.fmum.input.IInput;
import com.fmum.item.EquippedWrapper;
import com.fmum.item.IEquippedItem;
import com.fmum.item.IItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;

public class SEquippedTakeOut extends EquippedWrapper
{
	protected int tick_left;
	
	public SEquippedTakeOut( IEquippedItem wrapped, IItem item )
	{
		super( wrapped );
		
		final GunType type = ( GunType ) item.getType();
		this.tick_left = type.op_take_out.tick_count;
	}
	
	@Override
	public IEquippedItem tickInHand( IItem item, EnumHand hand, EntityPlayer player )
	{
		if ( this.tick_left == 0 ) {
			return this.wrapped.tickInHand( item, hand, player );
		}
		
//		final GunType type = ( GunType ) item.getType();
//		final GunOpConfig config = type.op_take_out;
//		final int tick_left = this.tick_left - 1;
//		this.tick_left = tick_left;
		this.tick_left -= 1;
		return this;
	}
	
	@Override
	public IEquippedItem onInputUpdate( IItem item, String name, IInput input )
	{
		return null;
	}
}
