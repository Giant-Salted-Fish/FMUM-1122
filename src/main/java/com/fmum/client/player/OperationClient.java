package com.fmum.client.player;

import com.fmum.client.render.IAnimator;
import com.fmum.common.IAutowirePacketHandler;
import com.fmum.common.item.IEquippedItem;
import com.fmum.common.network.PacketCode;
import com.fmum.common.network.PacketCode.Code;
import com.fmum.common.operation.IOperation;
import com.fmum.common.operation.IOperationController;
import com.fmum.common.operation.Operation;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public abstract class OperationClient< T extends IEquippedItem< ? > >
	extends Operation implements IAutowirePacketHandler
{
	protected T equipped;
	
	public OperationClient( T equipped, IOperationController controller )
	{
		super( controller );
		
		this.equipped = equipped;
	}
	
	@Override
	public IOperation launch( EntityPlayer player )
	{
		this.launchCallback();
		return this;
	}
	
	@Override
	@SuppressWarnings( "unchecked" )
	public IOperation onStackUpdate( IEquippedItem< ? > newEquipped, EntityPlayer player )
	{
		this.equipped = ( T ) newEquipped;
		return this;
	}
	
	@Override
	public IOperation terminate( EntityPlayer player )
	{
		PlayerPatchClient.instance.camera.useAnimation( IAnimator.NONE );
		this.sendPacketToServer( new PacketCode( Code.TERMINATE_OP ) );
		this.endCallback();
		return NONE;
	}
	
	@Override
	protected IOperation onComplete( EntityPlayer player )
	{
		PlayerPatchClient.instance.camera.useAnimation( IAnimator.NONE );
		this.endCallback();
		return NONE;
	}
	
	protected void launchCallback() { }
	
	protected void endCallback() { }
}
