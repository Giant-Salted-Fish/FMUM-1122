package com.fmum.client.player;

import com.fmum.client.FMUMClient;
import com.fmum.client.render.IAnimator;
import com.fmum.common.network.PacketTerminateOp;
import com.fmum.common.player.IOperation;
import com.fmum.common.player.Operation;
import com.fmum.common.player.OperationController;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public abstract class OperationClient< C extends OperationController >
	extends Operation< C >
{
	public OperationClient( C controller ) { super( controller ); }
	
	@Override
	public IOperation terminate( EntityPlayer player )
	{
		PlayerPatchClient.instance.camera.useAnimation( IAnimator.NONE );
		FMUMClient.sendPacketToServer( new PacketTerminateOp() );
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
