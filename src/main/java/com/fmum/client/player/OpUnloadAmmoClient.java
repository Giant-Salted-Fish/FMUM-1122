package com.fmum.client.player;

import com.fmum.common.gun.IEquippedMag;
import com.fmum.common.gun.IMag;
import com.fmum.common.operation.IOperation;
import com.fmum.common.operation.IOperationController;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class OpUnloadAmmoClient extends OperationClient< IEquippedMag< ? > >
{
	public OpUnloadAmmoClient( IEquippedMag< ? > mag, IOperationController controller ) {
		super( mag, controller );
	}
	
	@Override
	public IOperation launch( EntityPlayer player )
	{
		final IMag< ? > mag = this.equipped.item();
		if ( mag.isEmpty() ) { return NONE; }
		
		this.clearProgress();
		return super.launch( player );
	}
	
	@Override
	protected IOperation onComplete( EntityPlayer player )
	{
		super.onComplete( player );
		
		this.clearProgress();
		return this.launch( player );
	}
}
