package com.mcwb.common.player;

import com.mcwb.common.gun.IGun;
import com.mcwb.common.gun.IMag;
import com.mcwb.common.item.IItem;
import com.mcwb.common.operation.IOperation;
import com.mcwb.common.operation.Operation;

import net.minecraft.entity.player.EntityPlayer;

public class OpUnloadMag extends Operation< IGun< ? > >
{
	public OpUnloadMag( EntityPlayer player, IGun< ? > gun ) {
		super( player, gun, gun.unloadMagController() );
	}
	
	@Override
	public IOperation launch( IOperation oldOp ) { return this.contexted.hasMag() ? this : NONE; }
	
	@Override
	public IOperation onInHandStackChange( IInUseItem newItem )
	{
		this.contexted = ( IGun< ? > ) newItem;
		return this.contexted.hasMag() ? this : this.terminate();
	}
	
	@Override
	protected void doHandleEffect()
	{
		final IMag< ? > mag = this.contexted.unloadMag();
		this.player.addItemStackToInventory( mag.toStack() );
	}
}
