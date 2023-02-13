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

public class OpLoadAmmo extends Operation< IMag >
{
	protected final int invSlot;
	
	protected IOperation next = NONE;
	
	public OpLoadAmmo( EntityPlayer player, IMag contexted, int invSlot )
	{
		super( player, contexted, contexted.pushAmmoController() );
		
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
			final IItemType type = IItemTypeHost.getType( stack );
			final boolean isAmmo = type instanceof IAmmoType;
			if( !isAmmo || !this.contexted.isAllowed( ( IAmmoType ) type ) ) break;
			
			return super.launch( oldOp );
		}
		return NONE;
	}
	
	@Override
	protected IOperation onComplete()
	{
		switch( 0 )
		{
		default:
			final InventoryPlayer inv = this.player.inventory;
			final ItemStack stack = inv.getStackInSlot( this.invSlot );
			final IItemType type = IItemTypeHost.getType( stack );
			if( !( type instanceof IAmmoType ) ) break;
			
			final IAmmoType ammo = ( IAmmoType ) type;
			if( !this.contexted.isAllowed( ammo ) ) break;
			
			this.contexted.pushAmmo( ammo );
			stack.shrink( 1 );
		}
		return this.next.launch( this );
	}
	
	@Override
	public IOperation onOtherTryLaunch( IOperation op )
	{
		this.next = op;
		return this;
	}
	
	@Override
	public IOperation onHoldingStackChange( IItem newItem )
	{
		if( newItem.meta() != this.contexted.meta() || ( ( IMag ) newItem ).isFull() )
			return this.terminate();
		
		this.contexted = ( IMag ) newItem;
		return this;
	}
}
