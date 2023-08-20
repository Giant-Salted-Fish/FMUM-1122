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
	
	private static final IMessageHandler< Packet, IMessage >
		C2S_HANDLER = ( msg, ctx ) -> {
			msg.handleServerSide( ctx );
			return null;
		},
		S2C_HANDLER = ( msg, ctx ) -> {
			msg.handleClientSide( ctx );
			return null;
		};
	
	public void regisPackets()
	{
		final boolean is_replay_attack = this.discriminator > 0;
		if ( is_replay_attack ) {
			return;
		}
		
//		this.__regisPacket( PacketTerminateOp.class, Side.SERVER );
//		this.__regisPacket( PacketNotifyEquipped.class, Side.SERVER );
//		this.__regisPacket( PacketModify.class, Side.SERVER );
//		this.__regisPacket( PacketGunShoot.class, Side.SERVER );
		
		this.__regisPacket( PacketConfigSync.class, Side.CLIENT );
	}
	
	private void __regisPacket(
		Class< ? extends Packet > packet_class, Side handle_on_side
	) {
		final boolean is_server_side = handle_on_side.isServer();
		final IMessageHandler< Packet, IMessage > handler =
			is_server_side ? C2S_HANDLER : S2C_HANDLER;
		this.registerMessage(
			handler, packet_class, this.discriminator, handle_on_side );
		this.discriminator += 1;
	}
}
