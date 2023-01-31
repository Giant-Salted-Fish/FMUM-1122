package com.mcwb.common;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public interface IAutowirePacketHandler
{
	public default void sendToServer( IMessage message ) { MCWB.NET.sendToServer( message ); }
	
	public default void sendTo( IMessage message, EntityPlayerMP player ) {
		MCWB.NET.sendTo( message, player );
	}
}
