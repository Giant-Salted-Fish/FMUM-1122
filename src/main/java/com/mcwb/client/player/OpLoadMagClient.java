package com.mcwb.client.player;

import com.mcwb.common.gun.IEquippedGun;
import com.mcwb.common.gun.IGun;
import com.mcwb.common.gun.IMag;
import com.mcwb.common.item.IEquippedItem;
import com.mcwb.common.item.IItem;
import com.mcwb.common.item.IItemTypeHost;
import com.mcwb.common.operation.IOperation;
import com.mcwb.common.operation.IOperationController;
import com.mcwb.util.Animation;

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
		Animation animtion
	) { super( gun, controller, animtion ); }
	
	@Override
	public IOperation launch( EntityPlayer player )
	{
		switch( 0 )
		{
		default:
			if( this.equipped.item().hasMag() ) break;
			
			this.invSlot = this.getValidMagInvSlot( player );
			if( this.invSlot == -1 ) break;
			
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
		return this.equipped.item().hasMag() ? this.terminate( player ) : this;
	}
	
	@Override
//	protected void doHandleEffect( EntityPlayer player )
	protected IOperation onComplete( EntityPlayer player )
	{
		// Calling install will change the state of the mag itself, hence copy before use
		final ItemStack stack = player.inventory.getStackInSlot( this.invSlot ).copy();
		final IItem item = IItemTypeHost.getItemOrDefault( stack );
		if( !( item instanceof IMag< ? > ) ) return NONE;
		
		final IMag< ? > mag = ( IMag< ? > ) item;
		final IGun< ? > gun = this.equipped.item();
		if( gun.isAllowed( mag ) ) gun.loadMag( mag );
		return NONE;
	}
	
	protected int getValidMagInvSlot( EntityPlayer player )
	{
		final IGun< ? > gun = this.equipped.item();
		final InventoryPlayer inv = player.inventory;
		final int size = inv.getSizeInventory();
		for( int i = 0; i < size; ++i )
		{
			final ItemStack stack = inv.getStackInSlot( i );
			final IItem item = IItemTypeHost.getItemOrDefault( stack );
			if( item instanceof IMag< ? > && gun.isAllowed( ( IMag< ? > ) item ) )
				return i;
		}
		return -1;
	}
}
