package com.mcwb.common.player;

import com.mcwb.common.gun.IEquippedGun;
import com.mcwb.common.gun.IGun;
import com.mcwb.common.gun.IMag;
import com.mcwb.common.item.IEquippedItem;
import com.mcwb.common.operation.IOperation;
import com.mcwb.common.operation.IOperationController;
import com.mcwb.common.operation.Operation;

import net.minecraft.entity.player.EntityPlayer;

public class OpUnloadMag extends Operation< IEquippedGun< ? > >
{
	public OpUnloadMag( IEquippedGun< ? > gun, IOperationController controller ) {
		super( gun, controller );
	}
	
	@Override
	public IOperation launch( EntityPlayer player )
	{
		final IGun< ? > gun = this.equipped.item();
		return gun.hasMag() ? this : NONE;
	}
	
	@Override
	public IOperation onStackUpdate( IEquippedItem< ? > newEquipped, EntityPlayer player )
	{
		// FIXME: It seems that the itemstack in server side will actually never change
		this.equipped = ( IEquippedGun< ? > ) newEquipped;
		final IGun< ? > gun = this.equipped.item();
		return gun.hasMag() ? this : this.terminate( player );
	}
	
	@Override
	protected void doHandleEffect( EntityPlayer player )
	{
		final IGun< ? > gun = this.equipped.item();
		final IMag< ? > mag = gun.unloadMag();
		player.addItemStackToInventory( mag.toStack() );
	}
}
