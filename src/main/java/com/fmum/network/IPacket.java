package com.fmum.network;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IPacket extends IMessage
{
	void handleServerSide( MessageContext ctx );
	
	@SideOnly( Side.CLIENT )
	void handleClientSide( MessageContext ctx );
}
