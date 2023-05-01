package com.fmum.common.player;

import com.fmum.common.gun.IGun;
import com.fmum.common.gun.IMag;
import com.fmum.common.item.IItem;
import com.fmum.common.item.IItemTypeHost;
import com.fmum.common.operation.IOperation;
import com.fmum.common.operation.IOperationController;
import com.fmum.common.operation.Operation;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

public class OpLoadMag extends Operation
{
	protected final IGun< ? > gun;
	
	protected final int invSlot;
	
	public OpLoadMag( IGun< ? > gun, IOperationController controller, int invSlot )
	{
		super( controller );
		
		this.gun = gun;
		this.invSlot = invSlot;
	}
	
	@Override
	public IOperation launch( EntityPlayer player )
	{
		if ( this.gun.hasMag() ) { return NONE; }
		
		final ItemStack stack = player.inventory.getStackInSlot( this.invSlot );
		final IItem item = IItemTypeHost.getItemOrDefault( stack );
		final boolean isMag = item instanceof IMag< ? >;
		final boolean isValidMag = isMag && this.gun.isAllowed( ( IMag< ? > ) item );
		if ( !isValidMag ) { return NONE; }
		
		return this;
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
		if ( !this.gun.isAllowed( mag ) ) { return; }
		
		this.gun.loadMag( mag );
		inv.setInventorySlotContents( this.invSlot, ItemStack.EMPTY );
	}
}
