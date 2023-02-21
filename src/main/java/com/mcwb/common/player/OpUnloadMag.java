package com.mcwb.common.player;

import com.mcwb.common.gun.IGun;
import com.mcwb.common.gun.IMag;
import com.mcwb.common.item.IItem;
import com.mcwb.common.operation.IOperation;
import com.mcwb.common.operation.Operation;

import net.minecraft.entity.player.EntityPlayer;

public class OpUnloadMag extends Operation< IGun >
{
	public OpUnloadMag( EntityPlayer player, IGun gun ) {
		super( player, gun, gun.unloadMagController() );
	}
	
	@Override
	public IOperation launch( IOperation oldOp ) {
		return this.contexted.hasMag() ? super.launch( oldOp ) : NONE;
	}
	
	@Override
	public IOperation onHoldingStackChange( IItem newItem )
	{
		if( !( ( IGun ) newItem ).hasMag() )
			return this.terminate();
		
		this.contexted = ( IGun ) newItem;
		return this;
	}
	
	@Override
	protected void dohandleEffect()
	{
		final IMag mag = this.contexted.unloadMag();
		this.player.addItemStackToInventory( mag.toStack() );
	}
}
