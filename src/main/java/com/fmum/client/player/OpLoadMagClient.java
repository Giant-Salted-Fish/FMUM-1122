package com.fmum.client.player;

import com.fmum.common.gun.IEquippedGun;
import com.fmum.common.gun.IGun;
import com.fmum.common.gun.IMag;
import com.fmum.common.item.IItem;
import com.fmum.common.item.IItemTypeHost;
import com.fmum.common.operation.IOperation;
import com.fmum.common.operation.IOperationController;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public abstract class OpLoadMagClient extends OperationClient< IEquippedGun< ? > >
{
	protected int invSlot;
	
	public OpLoadMagClient( IEquippedGun< ? > gun, IOperationController controller ) {
		super( gun, controller );
	}
	
	@Override
	public IOperation launch( EntityPlayer player )
	{
		final IGun< ? > gun = this.equipped.item();
		if ( gun.hasMag() ) { return NONE; }
		
		this.invSlot = this.getValidMagInvSlot( player );
		final boolean validMagNotFound = this.invSlot == -1;
		if ( validMagNotFound ) { return NONE; }
		
		return super.launch( player );
	}
	
	protected final int getValidMagInvSlot( EntityPlayer player )
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
