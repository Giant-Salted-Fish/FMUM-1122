package com.fmum.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public abstract class DataModuleLoc implements FMUMPacket
{
	protected byte[] loc;
	
	public DataModuleLoc() { }
	
	public DataModuleLoc(byte[] loc, int locLen)
	{
		this.loc = new byte[locLen];
		System.arraycopy(loc, 0, this.loc, 0, locLen);
	}
	
	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf data)
	{
		data.writeByte(this.loc.length >>> 1);
		for(int i = this.loc.length; i-- > 0; data.writeByte(this.loc[i]));
	}
	
	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf data)
	{
		this.loc = new byte[(0xFF & data.readByte()) << 1];
		for(int i = this.loc.length; i-- > 0; this.loc[i] = data.readByte());
	}
}
