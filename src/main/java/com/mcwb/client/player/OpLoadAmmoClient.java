package com.mcwb.client.player;

import com.mcwb.client.MCWBClient;
import com.mcwb.client.input.InputHandler;
import com.mcwb.common.IAutowirePacketHandler;
import com.mcwb.common.ammo.IAmmoType;
import com.mcwb.common.gun.IMag;
import com.mcwb.common.item.IItem;
import com.mcwb.common.item.IItemType;
import com.mcwb.common.item.IItemTypeHost;
import com.mcwb.common.network.PacketCode;
import com.mcwb.common.network.PacketCodeAssist;
import com.mcwb.common.network.PacketCodeAssist.Code;
import com.mcwb.common.operation.IOperation;
import com.mcwb.common.operation.Operation;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class OpLoadAmmoClient extends Operation< IMag > implements IAutowirePacketHandler
{
	protected int invSlot;
	
	public OpLoadAmmoClient() { super( null, null, null ); }
	
	public IOperation reset( IMag mag )
	{
		this.player = MCWBClient.MC.player;
		this.contexted = mag;
		this.controller = mag.pushAmmoController();
		return this;
	}
	
	@Override
	public IOperation launch( IOperation oldOp )
	{
		switch( 0 )
		{
		default:
			if( this.contexted.isFull() ) break;
			
			this.invSlot = this.getValidAmmoSlot( InputHandler.CO.down ? 1 : 0 );
			if( this.invSlot == -1 ) break;
			
			this.clearProgress();
			this.sendToServer( new PacketCodeAssist( Code.LOAD_AMMO, this.invSlot ) );
			return super.launch( oldOp );
		}
		
		return NONE;
	}
	
	@Override
	public IOperation terminate()
	{
		this.sendToServer( new PacketCode( PacketCode.Code.TERMINATE_OP ) );
		return NONE;
	}
	
	@Override
	public IOperation onHoldingStackChange( IItem newItem )
	{
		if( newItem.meta() != this.contexted.meta() || ( ( IMag ) newItem ).isFull() )
			return this.terminate();
		
		this.contexted = ( IMag ) newItem;
		return this;
	}
	
	@Override
	protected IOperation onComplete()
	{
		// Check again to make sure the ammo is still valid to load
		switch( 0 )
		{
		default:
			final ItemStack stack = this.player.inventory.getStackInSlot( this.invSlot );
			final IItemType type = IItemTypeHost.getType( stack );
			if( !( type instanceof IAmmoType ) ) break;
			
			final IAmmoType ammo = ( IAmmoType ) type;
			if( !this.contexted.isAllowed( ammo ) ) break;
			
			stack.shrink( 1 );
			this.contexted.pushAmmo( ammo );
			return this.launch( this );
		}
		
		return NONE;
	}
	
	protected int getValidAmmoSlot( int offset )
	{
		final InventoryPlayer inv = this.player.inventory;
		
		int invSlot = -1;
		for( int i = 0, size = inv.getSizeInventory(); offset >= 0 && i < size; ++i )
		{
			final ItemStack stack = inv.getStackInSlot( i );
			final IItemType type = IItemTypeHost.getType( stack );
			if( type instanceof IAmmoType && this.contexted.isAllowed( ( IAmmoType ) type ) )
			{
				invSlot = i;
				--offset;
			}
		}
		return invSlot;
	}
}
