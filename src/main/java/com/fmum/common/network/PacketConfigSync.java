package com.fmum.common.network;

import com.fmum.common.FMUM;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.entity.EntityPlayerSP;

public final class PacketConfigSync implements FMUMPacket
{
	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf data)
	{
		data.writeByte(FMUM.maxLayers);
	}
	
	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf data)
	{
		FMUM.maxLayers = data.readByte();
	}
	
	/**
	 * Override to disable error log client side
	 */
	@Override
	public void handleClientSide(EntityPlayerSP player) { }
}
