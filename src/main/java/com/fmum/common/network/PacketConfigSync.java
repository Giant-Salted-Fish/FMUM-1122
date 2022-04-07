package com.fmum.common.network;

import com.fmum.client.module.OpModification;
import com.fmum.common.CommonProxy;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class PacketConfigSync implements FMUMPacket
{
	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf data)
	{
		data.writeShort(CommonProxy.maxLocLen);
	}
	
	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf data)
	{
		CommonProxy.maxLocLen = data.readShort();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void handleClientSide(EntityPlayerSP player)
	{
		OpModification.INSTANCE.onConfigSync();
	}
}
