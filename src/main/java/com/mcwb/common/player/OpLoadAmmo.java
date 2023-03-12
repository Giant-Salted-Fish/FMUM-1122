package com.mcwb.common.player;

import com.mcwb.common.ammo.IAmmoType;
import com.mcwb.common.gun.IEquippedMag;
import com.mcwb.common.gun.IMag;
import com.mcwb.common.item.IEquippedItem;
import com.mcwb.common.item.IItemType;
import com.mcwb.common.item.IItemTypeHost;
import com.mcwb.common.operation.IOperation;
import com.mcwb.common.operation.Operation;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class OpLoadAmmo extends Operation< IEquippedMag< ? > >
{
	protected final int invSlot;
	
	protected IOperation next = NONE;
	
	public OpLoadAmmo( EntityPlayer player, IEquippedMag< ? > mag, int invSlot )
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
			final IMag< ? > mag = this.equipped.item();
			if( mag.isFull() ) break;
			
			final ItemStack stack = this.player.inventory.getStackInSlot( this.invSlot );
			final IItemType type = IItemTypeHost.getTypeOrDefault( stack.getItem() );
			final boolean isAmmo = type instanceof IAmmoType;
			if( !isAmmo || !mag.isAllowed( ( IAmmoType ) type ) ) break;
			
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
	public IOperation onStackUpdate( IEquippedItem< ? > newEquipped )
	{
		this.equipped = ( IEquippedMag< ? > ) newEquipped;
		return this.equipped.item().isFull() ? this.terminate() : this;
	}
	
	@Override
	protected IOperation onComplete() { return this.next.launch( this ); }
	
	@Override
	protected void doHandleEffect()
	{
		final ItemStack stack = this.player.inventory.getStackInSlot( this.invSlot );
		final IItemType type = IItemTypeHost.getTypeOrDefault( stack.getItem() );
		if( !( type instanceof IAmmoType ) ) return;
		
		final IAmmoType ammo = ( IAmmoType ) type;
		final IMag< ? > mag = this.equipped.item();
		if( !mag.isAllowed( ammo ) ) return;
		
		mag.pushAmmo( ammo );
//		if( !this.player.isCreative() )
			stack.shrink( 1 );
	}
}
