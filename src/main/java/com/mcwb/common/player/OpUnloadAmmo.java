package com.mcwb.common.player;

import com.mcwb.common.ammo.IAmmoType;
import com.mcwb.common.gun.IMag;
import com.mcwb.common.item.IItem;
import com.mcwb.common.operation.IOperation;
import com.mcwb.common.operation.Operation;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class OpUnloadAmmo extends Operation< IMag< ? > >
{
	protected IOperation next = NONE;
	
	public OpUnloadAmmo( EntityPlayer player, IMag< ? > mag ) {
		super( player, mag, mag.popAmmoController() );
	}
	
	@Override
	public IOperation launch( IOperation oldOp ) { return this.contexted.isEmpty() ? NONE : this; }
	
	@Override
	public IOperation onOtherTryLaunch( IOperation op )
	{
		this.next = op;
		return this;
	}
	
	@Override
	public IOperation onInHandStackChange( IItem newItem )
	{
		this.contexted = ( IMag< ? > ) newItem;
		return this.contexted.isEmpty() ? this.terminate() : this;
	}
	
	@Override
	protected IOperation onComplete() { return this.next.launch( this ); }
	
	@Override
	protected void doHandleEffect()
	{
		final IAmmoType ammo = this.contexted.popAmmo();
		final ItemStack stack = new ItemStack( ammo.item() );
		
		this.player.addItemStackToInventory( stack );
	}
}
