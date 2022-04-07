package com.fmum.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public abstract class DataOpCode implements FMUMPacket
{
	protected byte opCode;
	
	public DataOpCode() { }
	
	public DataOpCode(byte opCode) { this.opCode = opCode; }
	
	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {
		data.writeByte(this.opCode);
	}
	
	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {
		this.opCode = data.readByte();
	}
}
