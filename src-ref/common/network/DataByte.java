package com.fmum.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public abstract class DataByte implements FMUMPacket
{
	protected byte valueByte;
	
	public DataByte() { }
	
	public DataByte(byte val) { this.valueByte = val; }
	
	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {
		data.writeByte(this.valueByte);
	}
	
	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {
		this.valueByte = data.readByte();
	}
}
