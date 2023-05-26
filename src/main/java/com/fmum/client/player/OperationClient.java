package com.fmum.client.player;

import com.fmum.client.render.IAnimator;
import com.fmum.common.IAutowirePacketHandler;
import com.fmum.common.item.IEquippedItem;
import com.fmum.common.network.PacketTerminateOp;
import com.fmum.common.player.IOperation;
import com.fmum.common.player.Operation;
import com.fmum.common.player.OperationController;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public abstract class OperationClient< T extends IEquippedItem< ? >, C extends OperationController >
	extends Operation< C > implements IAutowirePacketHandler
{
	protected T equipped;
	
	public OperationClient( T equipped, C controller )
	{
		super( controller );
		
		this.equipped = equipped;
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
		this.sendPacketToServer( new PacketTerminateOp() );
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
	
	protected void endCallback() { }
}
