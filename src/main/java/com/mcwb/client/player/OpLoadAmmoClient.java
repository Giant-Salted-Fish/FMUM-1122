package com.mcwb.client.player;

import com.mcwb.client.input.InputHandler;
import com.mcwb.common.ammo.IAmmoType;
import com.mcwb.common.gun.IEquippedMag;
import com.mcwb.common.gun.IMag;
import com.mcwb.common.item.IItemType;
import com.mcwb.common.item.IItemTypeHost;
import com.mcwb.common.operation.IOperation;
import com.mcwb.common.operation.IOperationController;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class OpLoadAmmoClient extends OperationClient< IEquippedMag< ? > >
{
	protected int invSlot;
	
	public OpLoadAmmoClient( IEquippedMag< ? > mag, IOperationController controller ) {
		super( mag, controller );
	}
	
	@Override
	public IOperation launch( EntityPlayer player )
	{
		final IMag< ? > mag = this.equipped.item();
		if ( mag.isFull() ) { return NONE; }
		
		final int offset = InputHandler.CO.down ? 1 : 0;
		this.invSlot = this.getValidAmmoSlot( offset, player );
		final boolean validAmmoNotFound = this.invSlot == -1;
		if ( validAmmoNotFound ) { return NONE; }
		
		return super.launch( player );
	}
	
	@Override
	protected IOperation onComplete( EntityPlayer player )
	{
		super.onComplete( player );
		
		this.clearProgress();
		return this.launch( player );
	}
	
	protected final int getValidAmmoSlot( int offset, EntityPlayer player )
	{
		final InventoryPlayer inv = player.inventory;
		final IMag< ? > mag = this.equipped.item();
		
		int invSlot = -1;
		for ( int i = 0, size = inv.getSizeInventory(); offset >= 0 && i < size; ++i )
		{
			final ItemStack stack = inv.getStackInSlot( i );
			final IItemType type = IItemTypeHost.getTypeOrDefault( stack.getItem() );
			final boolean isAmmo = type instanceof IAmmoType;
			final boolean isValidAmmo = isAmmo && mag.isAllowed( ( IAmmoType ) type );
			if ( isValidAmmo )
			{
				invSlot = i;
				--offset;
			}
		}
		return invSlot;
	}
}
