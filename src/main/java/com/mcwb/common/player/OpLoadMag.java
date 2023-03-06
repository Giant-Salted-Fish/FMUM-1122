package com.mcwb.common.player;

import com.mcwb.common.gun.IGun;
import com.mcwb.common.gun.IMag;
import com.mcwb.common.item.IItem;
import com.mcwb.common.item.IItemTypeHost;
import com.mcwb.common.operation.IOperation;
import com.mcwb.common.operation.Operation;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

public class OpLoadMag extends Operation< IGun< ? > >
{
	protected final int invSlot;
	
	public OpLoadMag( EntityPlayer player, IGun< ? > gun, int invSlot )
	{
		super( player, gun, gun.loadMagController() );
		
		this.invSlot = invSlot;
	}
	
	@Override
	public IOperation launch( IOperation oldOp )
	{
		switch( 0 )
		{
		default:
			if( this.contexted.hasMag() ) break;
			
			final ItemStack stack = this.player.inventory.getStackInSlot( this.invSlot );
			final IItem item = IItemTypeHost.getTypeOrDefault( stack ).getContexted( stack );
			final boolean isMag = item instanceof IMag< ? >;
			if( !isMag || !this.contexted.isAllowed( ( IMag< ? > ) item ) ) break;
			
			return this;
		}
		return NONE;
	}
	
	@Override
	public IOperation onInHandStackChange( IItem newItem )
	{
		this.contexted = ( IGun< ? > ) newItem;
		return this.contexted.hasMag() ? NONE : this;
	}
	
	@Override
	protected void doHandleEffect()
	{
		final InventoryPlayer inv = this.player.inventory;
		final ItemStack stack = inv.getStackInSlot( this.invSlot );
		final IItem item = IItemTypeHost.getTypeOrDefault( stack ).getContexted( stack );
		final boolean isMag = item instanceof IMag< ? >;
		if( !isMag ) return;
		
		final IMag< ? > mag = ( IMag< ? > ) item;
		if( !this.contexted.isAllowed( mag ) ) return;
		
		this.contexted.loadMag( mag );
		inv.setInventorySlotContents( this.invSlot, ItemStack.EMPTY );
	}
}
