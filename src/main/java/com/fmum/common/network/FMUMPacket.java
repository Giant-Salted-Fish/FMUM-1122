package com.fmum.common.network;

import com.fmum.common.FMUM;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Copied from Flan's Mod
 * 
 * @author FlansGame
 */
public interface FMUMPacket
{
	/**
	 * Encode the packet into a ByteBuf stream. Advanced data handlers can be found at
	 * {@link net.minecraftforge.fml.common.network.ByteBufUtils}.
	 */
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf data);
	
	/**
	 * Decode the packet from a ByteBuf stream. Advanced data handlers can be found at
	 * {@link net.minecraftforge.fml.common.network.ByteBufUtils}.
	 */
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf data);
	
	/**
	 * Handle the packet on server side, post-decoding
	 */
	default public void handleServerSide(EntityPlayerMP player)
	{
		if(
			this.doHandleServerSide(
				player,
				player.inventory.getCurrentItem()
			) == EnumActionResult.SUCCESS
		) return;
		
		FMUM.log.error(
			FMUM.proxy.format(
				"fmum.unhandledpackets",
				this.getClass(),
				player.getName()
			)
		);
	}
	
	/**
	 * Handle the packet on client side, post-decoding
	 */
	@SideOnly(Side.CLIENT)
	default public void handleClientSide(EntityPlayerSP player)
	{
		if(
			this.doHandleClientSide(
				player,
				player.inventory.getCurrentItem()
			) == EnumActionResult.SUCCESS
		) return;
		
		FMUM.log.error(I18n.format("fmum.unhandledpacketc", this.getClass().getName()));
	}
	
	default public EnumActionResult doHandleServerSide(EntityPlayerMP player, ItemStack stack) {
		return EnumActionResult.PASS;
	}
	
	/**
	 * @return {@code true} if an error has occurred or it is not handled
	 */
	@SideOnly(Side.CLIENT)
	default public EnumActionResult doHandleClientSide(EntityPlayerSP player, ItemStack stack) {
		return EnumActionResult.PASS;
	}
	
	/**
	 * Convenient method to write strings
	 */
	public static void writeUTF(ByteBuf data, String s) { ByteBufUtils.writeUTF8String(data, s); }
	
	/**
	 * Convenient method to read stringsn
	 */
	public static String readUTF(ByteBuf data) { return ByteBufUtils.readUTF8String(data); }
}
