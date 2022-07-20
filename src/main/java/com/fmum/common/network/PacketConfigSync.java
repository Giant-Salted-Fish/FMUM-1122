package com.fmum.common.network;

import com.fmum.common.FMUM;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public final class PacketConfigSync implements Packet
{
	@Override
	public void encodeInto( ChannelHandlerContext ctx, ByteBuf data )
	{
		final FMUM mod = FMUM.MOD;
		
		data.writeByte( mod.maxLocLen / 2 );
		data.writeByte( mod.maxCanInstall );
	}
	
	@Override
	public void decodeInto( ChannelHandlerContext ctx, ByteBuf data )
	{
		final FMUM mod = FMUM.MOD;
		
		mod.maxLocLen = ( 0xFF & data.readByte() ) * 2;
		mod.maxCanInstall = 0xFF & data.readByte();
	}
}
