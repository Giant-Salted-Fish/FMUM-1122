package com.mcwb.client.player;

import com.mcwb.common.IAutowirePacketHandler;
import com.mcwb.common.item.IEquippedItem;
import com.mcwb.common.network.PacketCode;
import com.mcwb.common.network.PacketCode.Code;
import com.mcwb.common.operation.IOperation;
import com.mcwb.common.operation.IOperationController;
import com.mcwb.common.operation.Operation;
import com.mcwb.util.Animation;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public abstract class OperationClient< T extends IEquippedItem< ? > >
	extends Operation< T > implements IAutowirePacketHandler
{
	public OperationClient( T equipped, IOperationController controller ) {
		super( equipped, controller );
	}
	
	@Override
	public IOperation launch( EntityPlayer player )
	{
		this.launchCallback();
		return this;
	}
	
	@Override
	public IOperation terminate( EntityPlayer player )
	{
		PlayerPatchClient.instance.camera.useAnimation( Animation.NONE );
		this.equipped.animator().useAnimation( Animation.NONE );
		
		this.sendPacketToServer( new PacketCode( Code.TERMINATE_OP ) );
		this.endCallback();
		return NONE;
	}
	
	@Override
	protected IOperation onComplete( EntityPlayer player )
	{
		PlayerPatchClient.instance.camera.useAnimation( Animation.NONE );
		this.equipped.animator().useAnimation( Animation.NONE );
		
		this.endCallback();
		return NONE;
	}
	
	protected void launchCallback() { }
	
	protected void endCallback() { }
}
