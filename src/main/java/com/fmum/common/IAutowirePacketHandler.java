package com.fmum.common;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * Implement this if you need to send network packets.
 * 
 * @author Giant_Salted_Fish
 */
public interface IAutowirePacketHandler
{
	default void sendPacketToServer( IMessage message ) {
		FMUM.NET.sendToServer( message );
	}
	
	default void sendPacketTo( IMessage message, EntityPlayerMP player ) {
		FMUM.NET.sendTo( message, player );
	}
}
