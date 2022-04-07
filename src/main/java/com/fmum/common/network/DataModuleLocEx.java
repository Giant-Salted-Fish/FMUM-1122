package com.fmum.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class DataModuleLocEx extends DataModuleLoc
{
	protected int assist;
	
	public DataModuleLocEx() { }
	
	public DataModuleLocEx(byte[] loc, int locLen, int assist)
	{
		super(loc, locLen);
		
		this.assist = assist;
	}
	
	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf data)
	{
		super.encodeInto(ctx, data);
		
		data.writeInt(this.assist);
	}
	
	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf data)
	{
		super.decodeInto(ctx, data);
		
		this.assist = data.readInt();
	}
}
