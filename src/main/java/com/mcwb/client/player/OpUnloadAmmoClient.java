package com.mcwb.client.player;

import com.mcwb.client.MCWBClient;
import com.mcwb.common.IAutowirePacketHandler;
import com.mcwb.common.gun.IMag;
import com.mcwb.common.item.IItem;
import com.mcwb.common.network.PacketCode;
import com.mcwb.common.network.PacketCode.Code;
import com.mcwb.common.operation.IOperation;
import com.mcwb.common.operation.Operation;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class OpUnloadAmmoClient extends Operation< IMag > implements IAutowirePacketHandler
{
	public OpUnloadAmmoClient() { super( null, null, null ); }
	
	public IOperation reset( IMag mag )
	{
		this.player = MCWBClient.MC.player;
		this.contexted = mag;
		this.controller = mag.popAmmoController();
		return this;
	}
	
	@Override
	public IOperation launch( IOperation oldOp )
	{
		if( this.contexted.isEmpty() ) return NONE;
		
		this.clearProgress();
		this.sendToServer( new PacketCode( Code.UNLOAD_AMMO ) );
		return super.launch( oldOp );
	}
	
	@Override
	public IOperation terminate()
	{
		this.sendToServer( new PacketCode( Code.TERMINATE_OP ) );
		return NONE;
	}
	
	@Override
	public IOperation onHoldingStackChange( IItem newItem )
	{
		if( ( ( IMag ) newItem ).isEmpty() )
			return this.terminate();
		
		this.contexted = ( IMag ) newItem;
		return this;
	}
	
	@Override
	protected IOperation onComplete() { return this.launch( this ); }
	
	@Override
	protected void dohandleEffect() { this.contexted.popAmmo(); }
}
