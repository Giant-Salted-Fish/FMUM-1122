package com.fmum.common.network;

import com.fmum.common.player.PlayerPatch;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class PacketTerminateOp implements IPacket
{
	public PacketTerminateOp() { }
	
	@Override
	public void toBytes( ByteBuf buf ) { }
	
	@Override
	public void fromBytes( ByteBuf buf ) { }
	
	@Override
	public void handleServerSide( MessageContext ctx )
	{
		final EntityPlayerMP player = ctx.getServerHandler().player;
		player.getServerWorld().addScheduledTask(
			() -> PlayerPatch.get( player ).terminateExecuting()
		);
	}
}
