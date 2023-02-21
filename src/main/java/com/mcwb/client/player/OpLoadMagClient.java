package com.mcwb.client.player;

import com.mcwb.client.MCWBClient;
import com.mcwb.common.IAutowirePacketHandler;
import com.mcwb.common.gun.IGun;
import com.mcwb.common.gun.IMag;
import com.mcwb.common.item.IItem;
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
public class OpLoadMagClient extends Operation< IGun > implements IAutowirePacketHandler
{
	protected int invSlot;
	
	public OpLoadMagClient() { super( null, null, null ); }
	
	public IOperation reset( IGun gun )
	{
		this.player = MCWBClient.MC.player;
		this.contexted = gun;
		this.controller = gun.loadMagController();
		return this;
	}
	
	@Override
	public IOperation launch( IOperation oldOp )
	{
		switch( 0 )
		{
		default:
			if( this.contexted.hasMag() ) break;
			
			this.invSlot = this.getValidMagSlot();
			if( this.invSlot == -1 ) break;
			
			this.clearProgress();
			this.sendToServer( new PacketCodeAssist( Code.LOAD_MAG, this.invSlot ) );
			return super.launch( oldOp );
		}
		
		return NONE;
	}
	
	@Override
	public IOperation terminate()
	{
		this.sendToServer( new PacketCode( PacketCode.Code.TERMINATE_OP ) );
		return super.terminate();
	}
	
	@Override
	public IOperation onHoldingStackChange( IItem newItem )
	{
		if( ( ( IGun ) newItem ).hasMag() )
			return this.terminate();
		
		this.contexted = ( IGun ) newItem;
		return null;
	}
	
	@Override
	protected void dohandleEffect()
	{
		final ItemStack stack = this.player.inventory.getStackInSlot( this.invSlot );
		final IItem item = IItemTypeHost.getType( stack ).getContexted( stack );
		if( !( item instanceof IMag ) ) return;
		
		final IMag mag = ( IMag ) item;
		if( this.contexted.isAllowed( mag ) )
			this.contexted.loadMag( mag );
	}
	
	protected int getValidMagSlot()
	{
		final InventoryPlayer inv = this.player.inventory;
		for( int i = 0, size = inv.getSizeInventory(); i < size; ++i )
		{
			final ItemStack stack = inv.getStackInSlot( i );
			final IItem item = IItemTypeHost.getType( stack ).getContexted( stack );
			if( item instanceof IMag && this.contexted.isAllowed( ( IMag ) item ) )
				return i;
		}
		return -1;
	}
}
