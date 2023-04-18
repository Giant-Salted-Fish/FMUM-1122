package com.mcwb.client.player;

import java.util.function.Consumer;

import com.mcwb.common.gun.IEquippedGun;
import com.mcwb.common.gun.IGun;
import com.mcwb.common.gun.IMag;
import com.mcwb.common.item.IEquippedItem;
import com.mcwb.common.item.IItem;
import com.mcwb.common.item.IItemTypeHost;
import com.mcwb.common.operation.IOperation;
import com.mcwb.common.operation.IOperationController;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class OpLoadMagClient extends OperationClient< IEquippedGun< ? > >
{
	protected int invSlot;
	
	public OpLoadMagClient(
		IEquippedGun< ? > gun,
		IOperationController controller,
		Runnable launchCallback,
		Consumer< IEquippedGun< ? > > ternimateCallback
	) { super( gun, controller, launchCallback, ternimateCallback ); }
	
	@Override
	public IOperation launch( EntityPlayer player )
	{
		switch ( 0 )
		{
		default:
			if ( this.equipped.item().hasMag() ) { break; }
			
			this.invSlot = this.getValidMagInvSlot( player );
			if ( this.invSlot == -1 ) { break; }
			
			this.clearProgress();
//			this.sendToServer( message );
			return super.launch( player );
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
	
//	@Override
//	protected void doHandleEffect( EntityPlayer player )
//	{
//		// Calling install will change the state of the mag itself, hence copy before use
//		final ItemStack stack = player.inventory.getStackInSlot( this.invSlot ).copy();
//		final IItem item = IItemTypeHost.getItemOrDefault( stack );
//		final boolean isMag = item instanceof IMag< ? >;
//		if ( !isMag ) { return; }
//		
//		final IMag< ? > mag = ( IMag< ? > ) item;
//		final IGun< ? > gun = this.equipped.item();
//		if ( gun.isAllowed( mag ) ) { gun.loadMag( mag ); }
//	}
	
	protected int getValidMagInvSlot( EntityPlayer player )
	{
		final IGun< ? > gun = this.equipped.item();
		final InventoryPlayer inv = player.inventory;
		final int size = inv.getSizeInventory();
		for ( int i = 0; i < size; ++i )
		{
			final ItemStack stack = inv.getStackInSlot( i );
			final IItem item = IItemTypeHost.getItemOrDefault( stack );
			final boolean isMag = item instanceof IMag< ? >;
			final boolean isValidMag = isMag && gun.isAllowed( ( IMag< ? > ) item );
			if ( isValidMag ) { return i; }
		}
		return -1;
	}
}
