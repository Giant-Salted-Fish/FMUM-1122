package com.mcwb.common.player;

import com.mcwb.common.ammo.IAmmoType;
import com.mcwb.common.gun.IMag;
import com.mcwb.common.operation.IOperation;
import com.mcwb.common.operation.IOperationController;
import com.mcwb.common.operation.Operation;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class OpUnloadAmmo extends Operation
{
	protected final IMag< ? > mag;
	
	protected IOperation next = NONE;
	
	public OpUnloadAmmo( IMag< ? > mag, IOperationController controller )
	{
		super( controller );
		
		this.mag = mag;
	}
	
	@Override
	public IOperation launch( EntityPlayer player ) { return this.mag.isEmpty() ? NONE : this; }
	
	@Override
	public IOperation onOtherTryLaunch( IOperation op, EntityPlayer player )
	{
		this.next = op;
		return this;
	}
	
	@Override
	protected IOperation onComplete( EntityPlayer player ) { return this.next.launch( player ); }
	
	@Override
	protected void doHandleEffect( EntityPlayer player )
	{
		final IAmmoType ammo = this.mag.popAmmo();
		final ItemStack stack = new ItemStack( ammo.item() );
		player.addItemStackToInventory( stack );
	}
}
