package com.mcwb.common.player;

import com.mcwb.common.ammo.IAmmoType;
import com.mcwb.common.gun.IMag;
import com.mcwb.common.item.IItem;
import com.mcwb.common.item.IItemType;
import com.mcwb.common.item.IItemTypeHost;
import com.mcwb.common.operation.IOperation;
import com.mcwb.common.operation.Operation;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

public class OpLoadAmmo extends Operation< IMag< ? > >
{
	protected final int invSlot;
	
	protected IOperation next = NONE;
	
	public OpLoadAmmo( EntityPlayer player, IMag< ? > mag, int invSlot )
	{
		super( player, mag, mag.pushAmmoController() );
		
		this.invSlot = invSlot;
	}
	
	@Override
	public IOperation launch( IOperation oldOp )
	{
		switch( 0 )
		{
		default:
			if( this.contexted.isFull() ) break;
			
			final ItemStack stack = this.player.inventory.getStackInSlot( this.invSlot );
			final IItemType type = IItemTypeHost.getTypeOrDefault( stack );
			final boolean isAmmo = type instanceof IAmmoType;
			if( !isAmmo || !this.contexted.isAllowed( ( IAmmoType ) type ) ) break;
			
			return this;
		}
		return NONE;
	}
	
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
		return this.contexted.isFull() ? this.terminate() : this;
	}
	
	@Override
	protected IOperation onComplete() { return this.next.launch( this ); }
	
	@Override
	protected void doHandleEffect()
	{
		final InventoryPlayer inv = this.player.inventory;
		final ItemStack stack = inv.getStackInSlot( this.invSlot );
		final IItemType type = IItemTypeHost.getTypeOrDefault( stack );
		if( !( type instanceof IAmmoType ) ) return;
		
		final IAmmoType ammo = ( IAmmoType ) type;
		if( !this.contexted.isAllowed( ammo ) ) return;
		
		this.contexted.pushAmmo( ammo );
//		if( !this.player.isCreative() )
			stack.shrink( 1 );
	}
}
