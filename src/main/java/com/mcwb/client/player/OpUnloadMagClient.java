package com.mcwb.client.player;

import java.util.function.Consumer;

import com.mcwb.common.gun.IEquippedGun;
import com.mcwb.common.gun.IGun;
import com.mcwb.common.item.IEquippedItem;
import com.mcwb.common.operation.IOperation;
import com.mcwb.common.operation.IOperationController;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class OpUnloadMagClient extends OperationClient< IEquippedGun< ? > >
{
	public OpUnloadMagClient(
		IEquippedGun< ? > gun,
		IOperationController controller,
		Runnable launchCallback,
		Consumer< IEquippedGun< ? > > ternimateCallback
	) { super( gun, controller, launchCallback, ternimateCallback ); }
	
	@Override
	public IOperation launch( EntityPlayer player )
	{
		final IGun< ? > gun = this.equipped.item();
		return gun.hasMag() ? super.launch( player ) : NONE;
	}
	
	@Override
	public IOperation onStackUpdate( IEquippedItem< ? > newEquipped, EntityPlayer player )
	{
		this.equipped = ( IEquippedGun< ? > ) newEquipped;
		return this;
	}
}
