package com.fmum.client.player;

import com.fmum.common.gun.IEquippedGun;
import com.fmum.common.gun.IGun;
import com.fmum.common.operation.IOperation;
import com.fmum.common.operation.IOperationController;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public abstract class OpUnloadMagClient extends OperationClient< IEquippedGun< ? > >
{
	public OpUnloadMagClient( IEquippedGun< ? > gun, IOperationController controller ) {
		super( gun, controller );
	}
	
	@Override
	public IOperation launch( EntityPlayer player )
	{
		final IGun< ? > gun = this.equipped.item();
		if ( !gun.hasMag() ) { return NONE; }
		
		return super.launch( player );
	}
}
