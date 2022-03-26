package com.fmum.common.network;

import com.fmum.common.FMUM;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;

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
	
	@Override
	public void handleClientSide(EntityPlayer player) { }
}
