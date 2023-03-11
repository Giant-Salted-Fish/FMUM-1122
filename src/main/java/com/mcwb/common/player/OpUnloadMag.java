package com.mcwb.common.player;

import com.mcwb.common.gun.IEquippedGun;
import com.mcwb.common.gun.IMag;
import com.mcwb.common.item.IEquippedItem;
import com.mcwb.common.operation.IOperation;
import com.mcwb.common.operation.Operation;

import net.minecraft.entity.player.EntityPlayer;

public class OpUnloadMag extends Operation< IEquippedGun >
{
	public OpUnloadMag( EntityPlayer player, IEquippedGun gun ) {
		super( player, gun, gun.unloadMagController() );
	}
	
	@Override
	public IOperation launch( IOperation oldOp ) {
		return this.equipped.item().hasMag() ? this : NONE;
	}
	
	@Override
	public IOperation onInHandStackChange( IEquippedItem newItem )
	{
		this.equipped = ( IEquippedGun ) newItem;
		return this.equipped.item().hasMag() ? this : this.terminate();
	}
	
	@Override
	protected void doHandleEffect()
	{
		final IMag< ? > mag = this.equipped.item().unloadMag();
		this.player.addItemStackToInventory( mag.toStack() );
	}
}
