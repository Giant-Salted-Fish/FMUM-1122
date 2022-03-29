package com.fmum.common.network;

import com.fmum.common.FMUM;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface PacketItemOp extends FMUMPacket
{
	public int getOpCode();
	
	public void setOpCode(byte opCode);
	
	@Override
	default void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {
		data.writeByte(this.getOpCode());
	}
	
	@Override
	default void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {
		this.setOpCode(data.readByte());
	}
	
	@Override
	default public void handleServerSide(EntityPlayerMP player)
	{
		FMUM.log.error(
			FMUM.proxy.format(
				"fmum.unhandleditemoppackets",
				this.getClass().getName(),
				Integer.toString(this.getOpCode()),
				player.getName()
			)
		);
		// TODO: do proper record
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	default public void handleClientSide(EntityPlayerSP player)
	{	
		FMUM.log.error(
			I18n.format(
				"fmum.unhandleditemoppacketc",
				this.getClass().getName(),
				Integer.toString(this.getOpCode())
			)
		);
	}
}
