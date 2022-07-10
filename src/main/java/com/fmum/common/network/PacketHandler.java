package com.fmum.common.network;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;

import com.fmum.client.FMUMClient;
import com.fmum.common.FMUM;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Copied from Flan's Mod
 * 
 * @author
 *     FlansGame with much inspiration from http://www.minecraftforge.net/wiki/Netty_Packet_Handling
 */
@ChannelHandler.Sharable
public class PacketHandler extends MessageToMessageCodec<FMLProxyPacket, Packet>
{
	/**
	 * Map of channels for each side
	 */
	private EnumMap< Side, FMLEmbeddedChannel > channels;
	
	/**
	 * A list of the constructors of registered packets. No more than 256.
	 */
	private ArrayList< Constructor< ? extends Packet > > packets = new ArrayList<>();
	
	/**
	 * Used to retrieve id from the packet passed in
	 */
	private HashMap< Class< ? extends Packet >, Integer > packetIdMap = new HashMap<>();
	
	/**
	 * Whether or not the mod has initialized yet. Once true, no more packets may be registered.
	 */
	private boolean modInitialized = false;
	
	/**
	 * Registers a packet with the handler
	 * 
	 * @param packetClass Class of packet to register
	 */
	public void registerPacket( Class< ? extends Packet > packetClass )
	{
		if( this.packets.size() > 256 )
			throw new RuntimeException(
				"Packet amount exceeding limit 256 while attempting "
				+ "to register <" + packetClass.getName() + ">"
			);
		if( this.packetIdMap.containsKey( packetClass ) )
			throw new RuntimeException(
				"Packet <" + packetClass.getName() + "> has been registered twice"
			);
		if( this.modInitialized )
			throw new RuntimeException(
				"Tried to register packet <" + packetClass.getName() + "> after mod initialization"
			);
		
		this.packetIdMap.put( packetClass, this.packets.size() );
		try { this.packets.add( packetClass.getConstructor() ); }
		catch(NoSuchMethodException | SecurityException e)
		{
			throw new RuntimeException(
				"Fail to fetch constructor from packet class <" + packetClass.getName() + ">"
			);
		}
	}
	
	@Override
	protected void encode( ChannelHandlerContext ctx, Packet msg, List< Object > out )
		throws Exception
	{
		// Define a new buffer to store our data upon encoding
		ByteBuf encodedData = Unpooled.buffer();
		
		// Get the packet class
		Class< ? extends Packet > clazz = msg.getClass();
		
		// If this packet has not been registered by our handler, reject it
		Integer discriminator = this.packetIdMap.get( clazz );
		if( discriminator == null )
			throw new RuntimeException(
				"Try to encode packet that is not registered <" + clazz.getName() + ">"
			);
		
		// Like a packet ID. Stored as the first entry in the packet code for recognition
		encodedData.writeByte( discriminator );
		
		// Get the packet class to encode our packet
		msg.encodeInto( ctx, encodedData );
		
		// Convert our packet into a Forge packet to get it through the Netty system
		// Add our packet to the outgoing packet queue
		out.add(
			new FMLProxyPacket(
				new PacketBuffer( encodedData.copy() ),
				ctx.channel().attr( NetworkRegistry.FML_CHANNEL ).get()
			)
		);
	}
	
	@Override
	protected void decode( ChannelHandlerContext ctx, FMLProxyPacket msg, List< Object > out )
		throws Exception
	{
		// Get the encoded data from the incoming packet
		ByteBuf encodedData = msg.payload();
		
		// If this discriminator returns no class, reject it
		int discriminator = 0xFF & encodedData.readByte();
		if( discriminator >= this.packets.size() )
			throw new RuntimeException(
				"Meet unregistered discriminator <" + discriminator + "> while decoding packet"
			);
		
		// Create an empty packet and decode our packet data into it
		Packet packet = this.packets.get( discriminator ).newInstance();
		packet.decodeInto( ctx, encodedData.slice() );
		
		// Check the side and handle our packet accordingly
		// TODO: Flan 1122 delay the process to main thread tick, maybe due to the concurrent issue?
		switch( FMLCommonHandler.instance().getEffectiveSide() )
		{
		case CLIENT:
			packet.handleClientSide( FMUMClient.mc.player );
			break;
		case SERVER:
			packet.handleServerSide(
				( ( NetHandlerPlayServer ) ctx.channel().attr(
					NetworkRegistry.NET_HANDLER
				).get() ).player
			);
		default:;
		}
	}
	
	public void init()
	{
		this.channels = NetworkRegistry.INSTANCE.newChannel( FMUM.MODID, this );
		
		// Register packets
//		this.registerPacket( PacketConfigSync.class );
//		this.registerPacket( PacketModuleTagInit.class );
//		this.registerPacket( PacketModuleInstall.class );
//		this.registerPacket( PacketModuleRemove.class );
//		this.registerPacket( PacketModuleUpdate.class );
	}
	
	public void postInit() { this.modInitialized = true; }
	
	/**
	 * Send a packet to a player
	 */
	public void sendTo( Packet packet, EntityPlayerMP player )
	{
		this.channels.get( Side.SERVER ).attr(
			FMLOutboundHandler.FML_MESSAGETARGET
		).set( FMLOutboundHandler.OutboundTarget.PLAYER );
		this.channels.get( Side.SERVER ).attr(
			FMLOutboundHandler.FML_MESSAGETARGETARGS
		).set( player );
		this.channels.get( Side.SERVER ).writeAndFlush( packet );
	}
	
	/**
	 * Send a packet to all players
	 */
	public void sendToAll( Packet packet )
	{
		this.channels.get( Side.SERVER ).attr(
			FMLOutboundHandler.FML_MESSAGETARGET
		).set( FMLOutboundHandler.OutboundTarget.ALL );
		this.channels.get( Side.SERVER ).writeAndFlush( packet );
	}
	
	/**
	 * Send a packet to all around a point
	 */
	public void sendToAllAround( Packet packet, NetworkRegistry.TargetPoint point )
	{
		this.channels.get( Side.SERVER ).attr(
			FMLOutboundHandler.FML_MESSAGETARGET
		).set( FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT );
		this.channels.get( Side.SERVER ).attr(
			FMLOutboundHandler.FML_MESSAGETARGETARGS
		).set( point );
		this.channels.get( Side.SERVER ).writeAndFlush( packet );
	}
	
	/**
	 * Send a packet to all in a dimension
	 */
	public void sendToDimension( Packet packet, int dimensionID )
	{
		this.channels.get( Side.SERVER ).attr(
			FMLOutboundHandler.FML_MESSAGETARGET
		).set( FMLOutboundHandler.OutboundTarget.DIMENSION );
		this.channels.get( Side.SERVER ).attr(
			FMLOutboundHandler.FML_MESSAGETARGETARGS
		).set( dimensionID );
		this.channels.get( Side.SERVER ).writeAndFlush( packet );
	}
	
	/**
	 * Send a packet to the server
	 */
	public void sendToServer( Packet packet )
	{
		this.channels.get( Side.CLIENT ).attr(
			FMLOutboundHandler.FML_MESSAGETARGET
		).set( FMLOutboundHandler.OutboundTarget.TOSERVER );
		this.channels.get( Side.CLIENT ).writeAndFlush( packet );
	}
	
	/**
	 * Send a packet to all around a point without having to create one's own TargetPoint
	 */
	public void sendToAllAround(
		Packet packet,
		double x,
		double y,
		double z,
		double range,
		int dimension
	) {
		this.sendToAllAround(
			packet,
			new NetworkRegistry.TargetPoint(
				dimension,
				x, y, z,
				range
			)
		);
	}
	
	/// Vanilla packets follow ///
	
	/**
	 * Send a packet to all players
	 */
	public void sendToAll( net.minecraft.network.Packet< ? > packet )
	{
		FMLCommonHandler.instance().getMinecraftServerInstance()
			.getPlayerList().sendPacketToAllPlayers( packet );
	}
	
	/**
	 * Send a packet to a player
	 */
	public void sendTo( net.minecraft.network.Packet< ? > packet, EntityPlayerMP player ) {
		player.connection.sendPacket( packet );
	}
	
	/**
	 * Send a packet to all around a point
	 */
	public void sendToAllAround(
		net.minecraft.network.Packet< ? > packet,
		NetworkRegistry.TargetPoint point
	) {
		FMLCommonHandler.instance().getMinecraftServerInstance()
			.getPlayerList().sendToAllNearExcept(
				null,
				point.x, point.y, point.z,
				point.range,
				point.dimension,
				packet
			);
	}
	
	/**
	 * Send a packet to all in a dimension
	 */
	public void sendToDimension( net.minecraft.network.Packet< ? > packet, int dimensionID )
	{
		FMLCommonHandler.instance().getMinecraftServerInstance()
			.getPlayerList().sendPacketToAllPlayersInDimension(
				packet,
				dimensionID
			);
	}
	
	/**
	 * Send a packet to the server
	 */
	public void sendToServer( net.minecraft.network.Packet< ? > packet ) {
		Minecraft.getMinecraft().player.connection.sendPacket( packet );
	}
}
