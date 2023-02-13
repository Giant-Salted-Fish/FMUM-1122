package com.mcwb.common.network;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class PacketHandler extends SimpleNetworkWrapper
{
	/**
	 * Count for registered packets to help assign discriminator
	 */
	private int count = 0;
	
	public PacketHandler( String channel ) { super( channel ); }
	
	private static final IMessageHandler< IPacket, IMessage > S_HANDLER = ( msg, ctx ) -> {
		msg.handleServerSide( ctx );
		return null;
	};
	
	private static final IMessageHandler< IPacket, IMessage > C_HANDLER = ( msg, ctx ) -> {
		msg.handleClientSide( ctx );
		return null;
	};
	
	public void regisPackets()
	{
		this.regis( PacketModify.class, Side.SERVER );
		this.regis( PacketCode.class, Side.SERVER );
		this.regis( PacketCodeAssist.class, Side.SERVER );
		
		this.regis( PacketConfigSync.class, Side.CLIENT );
	}
	
	private void regis( Class< ? extends IPacket > packetClass, Side handleInSide )
	{
		this.registerMessage(
			handleInSide == Side.SERVER ? S_HANDLER : C_HANDLER,
			packetClass,
			this.count++,
			handleInSide
		);
	}
}
