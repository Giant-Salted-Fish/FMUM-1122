package com.fmum.common.network;

import java.util.EnumMap;

import io.netty.handler.codec.MessageToMessageCodec;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Copied from Flan's Mod
 * 
 * @author FlansGame
 */
public final class PacketHandler extends MessageToMessageCodec<FMLProxyPacket, PacketBase>
{
	/**
	 * Map of channels for each side
	 */
	private EnumMap<Side, FMLEmbeddedChannel> channels;
	
	/**
	 * A list of registered packets. Should contain no more than 256 packets.
	 */
}
