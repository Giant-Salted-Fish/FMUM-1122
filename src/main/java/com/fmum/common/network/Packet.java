package com.fmum.common.network;

import com.fmum.common.Launcher.AutowireLogger;

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
public interface Packet extends AutowireLogger
{
	/**
	 * Encode the packet into a ByteBuf stream. Advanced data handlers can be found at
	 * {@link net.minecraftforge.fml.common.network.ByteBufUtils}.
	 */
	public default void encodeInto( ChannelHandlerContext ctx, ByteBuf data ) { }
	
	/**
	 * Decode the packet from a ByteBuf stream. Advanced data handlers can be found at
	 * {@link net.minecraftforge.fml.common.network.ByteBufUtils}.
	 */
	public default void decodeInto( ChannelHandlerContext ctx, ByteBuf data ) { }
	
	/**
	 * Handle the packet on server side, post-decoding
	 */
	public default void handleServerSide( EntityPlayerMP player )
	{
		if(
			this.doHandleServerSide(
				player,
				player.inventory.getCurrentItem()
			) == EnumActionResult.SUCCESS
		) return;
		
		this.log().error(
			this.format(
				"fmum.unhandledpackets",
				this.getClass(),
				player.getName()
			)
		);
	}
	
	/**
	 * Handle the packet on client side, post-decoding
	 */
	@SideOnly( Side.CLIENT )
	public default void handleClientSide( EntityPlayerSP player )
	{
		if(
			this.doHandleClientSide(
				player,
				player.inventory.getCurrentItem()
			) == EnumActionResult.SUCCESS
		) return;
		
		this.log().error( I18n.format( "fmum.unhandledpacketc", this.getClass().getName() ) );
	}
	
	public default EnumActionResult doHandleServerSide( EntityPlayerMP player, ItemStack stack ) {
		return EnumActionResult.PASS;
	}
	
	/**
	 * @return {@code true} if an error has occurred or it is not handled
	 */
	@SideOnly( Side.CLIENT )
	public default EnumActionResult doHandleClientSide( EntityPlayerSP player, ItemStack stack ) {
		return EnumActionResult.PASS;
	}
	
	/**
	 * Convenient method to write strings
	 */
	public static void writeUTF( ByteBuf data, String s ) {
		ByteBufUtils.writeUTF8String( data, s );
	}
	
	/**
	 * Convenient method to read strings
	 */
	public static String readUTF( ByteBuf data ) { return ByteBufUtils.readUTF8String( data ); }
}
