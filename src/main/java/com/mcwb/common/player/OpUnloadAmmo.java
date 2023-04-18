package com.mcwb.common.player;

import com.mcwb.common.ammo.IAmmoType;
import com.mcwb.common.gun.IEquippedMag;
import com.mcwb.common.gun.IMag;
import com.mcwb.common.item.IEquippedItem;
import com.mcwb.common.operation.IOperation;
import com.mcwb.common.operation.IOperationController;
import com.mcwb.common.operation.Operation;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class OpUnloadAmmo extends Operation< IEquippedMag< ? > >
{
	protected IOperation next = NONE;
	
	public OpUnloadAmmo( IEquippedMag< ? > mag, IOperationController controller ) {
		super( mag, controller );
	}
	
	@Override
	public IOperation launch( EntityPlayer player ) {
		return this.equipped.item().isEmpty() ? NONE : this;
	}
	
	@Override
	public IOperation onOtherTryLaunch( IOperation op, EntityPlayer player )
	{
		this.next = op;
		return this;
	}
	
	@Override
	public IOperation onStackUpdate( IEquippedItem< ? > newEquipped, EntityPlayer player )
	{
		this.equipped = ( IEquippedMag< ? > ) newEquipped;
		final IMag< ? > mag = this.equipped.item();
		return mag.isEmpty() ? this.terminate( player ) : this;
	}
	
	@Override
	protected IOperation onComplete( EntityPlayer player ) { return this.next.launch( player ); }
	
	@Override
	protected void doHandleEffect( EntityPlayer player )
	{
		final IMag< ? > mag = this.equipped.item();
		final IAmmoType ammo = mag.popAmmo();
		final ItemStack stack = new ItemStack( ammo.item() );
		player.addItemStackToInventory( stack );
	}
}
