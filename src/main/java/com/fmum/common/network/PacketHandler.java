package com.fmum.common.network;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class PacketHandler extends SimpleNetworkWrapper
{
	/**
	 * Count for registered packets to help assign discriminator.
	 */
	private int discriminator = 0;
	
	public PacketHandler( String channel ) { super( channel ); }
	
	private static final IMessageHandler< IPacket, IMessage >
		S_HANDLER = ( msg, ctx ) -> {
			msg.handleServerSide( ctx );
			return null;
		},
		C_HANDLER = ( msg, ctx ) -> {
			msg.handleClientSide( ctx );
			return null;
		};
	
	public void regisPackets()
	{
//		this.regis( PacketTerminateOp.class, Side.SERVER );
//		this.regis( PacketNotifyEquipped.class, Side.SERVER );
//		this.regis( PacketModify.class, Side.SERVER );
//		this.regis( PacketGunShoot.class, Side.SERVER );
		
		this.__regis( PacketConfigSync.class, Side.CLIENT );
	}
	
	private void __regis( Class< ? extends IPacket > packet_class, Side handle_on_side )
	{
		final boolean is_server_side = handle_on_side.isServer();
		final IMessageHandler< IPacket, IMessage > handler = is_server_side ? S_HANDLER : C_HANDLER;
		this.registerMessage( handler, packet_class, this.discriminator, handle_on_side );
		this.discriminator += 1;
	}
}
