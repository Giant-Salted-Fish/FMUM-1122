package com.mcwb.common.player;

import com.mcwb.common.gun.IEquippedGun;
import com.mcwb.common.gun.IGun;
import com.mcwb.common.gun.IMag;
import com.mcwb.common.item.IEquippedItem;
import com.mcwb.common.item.IItem;
import com.mcwb.common.item.IItemTypeHost;
import com.mcwb.common.operation.IOperation;
import com.mcwb.common.operation.IOperationController;
import com.mcwb.common.operation.Operation;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

public class OpLoadMag extends Operation< IEquippedGun< ? > >
{
	protected final int invSlot;
	
	public OpLoadMag( IEquippedGun< ? > gun, IOperationController controller, int invSlot )
	{
		super( gun, controller );
		
		this.invSlot = invSlot;
	}
	
	@Override
	public IOperation launch( EntityPlayer player )
	{
		switch ( 0 )
		{
		default:
			final IGun< ? > gun = this.equipped.item();
			if ( gun.hasMag() ) { break; }
			
			final ItemStack stack = player.inventory.getStackInSlot( this.invSlot );
			final IItem item = IItemTypeHost.getItemOrDefault( stack );
			final boolean isMag = item instanceof IMag< ? >;
			final boolean isValidMag = isMag && gun.isAllowed( ( IMag< ? > ) item );
			if ( !isValidMag ) { break; }
			
			return this;
		}
		return NONE;
	}
	
	@Override
	public IOperation onStackUpdate( IEquippedItem< ? > newEquipped, EntityPlayer player )
	{
		this.equipped = ( IEquippedGun< ? > ) newEquipped;
		final IGun< ? > gun = this.equipped.item();
		return gun.hasMag() ? this.terminate( player ) : this;
	}
	
	@Override
	protected void doHandleEffect( EntityPlayer player )
	{
		final InventoryPlayer inv = player.inventory;
		final ItemStack stack = inv.getStackInSlot( this.invSlot );
		final IItem item = IItemTypeHost.getItemOrDefault( stack );
		final boolean isMag = item instanceof IMag< ? >;
		if ( !isMag ) { return; }
		
		final IMag< ? > mag = ( IMag< ? > ) item;
		final IGun< ? > gun = this.equipped.item();
		if ( !gun.isAllowed( mag ) ) { return; }
		
		gun.loadMag( mag );
		inv.setInventorySlotContents( this.invSlot, ItemStack.EMPTY );
	}
}
