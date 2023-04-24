package com.mcwb.common.player;

import com.mcwb.common.gun.IGun;
import com.mcwb.common.operation.IOperation;
import com.mcwb.common.operation.IOperationController;
import com.mcwb.common.operation.Operation;

import net.minecraft.entity.player.EntityPlayer;

public class OpUnloadMag extends Operation
{
	protected final IGun< ? > gun;
	
	public OpUnloadMag( IGun< ? > gun, IOperationController controller )
	{
		super( controller );
		
		this.gun = gun;
	}
	
	@Override
	public IOperation launch( EntityPlayer player ) { return this.gun.hasMag() ? this : NONE; }
	
	// FIXME: It seems that the itemstack in server side will actually never change
//	@Override
//	public IOperation onStackUpdate( IEquippedItem< ? > newEquipped, EntityPlayer player )
//	{
//		this.equipped = ( IEquippedGun< ? > ) newEquipped;
//		final IGun< ? > gun = this.equipped.item();
//		return gun.hasMag() ? this : this.terminate( player );
//	}
	
	@Override
	protected void doHandleEffect( EntityPlayer player ) {
		player.addItemStackToInventory( this.gun.unloadMag().toStack() );
	}
}
