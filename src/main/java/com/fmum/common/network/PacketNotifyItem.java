package com.fmum.common.network;

import java.util.function.Consumer;

import com.fmum.common.item.IEquippedItem;
import com.fmum.common.player.PlayerPatch;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class PacketNotifyItem implements IPacket
{
	private final Consumer< ByteBuf > encoder;
	
	private ByteBuf buf;
	
	public PacketNotifyItem() { this.encoder = null; }
	
	public PacketNotifyItem( Consumer< ByteBuf > encoder ) { this.encoder = encoder; }
	
	@Override
	public void toBytes( ByteBuf buf ) { this.encoder.accept( buf ); }
	
	@Override
	public void fromBytes( ByteBuf buf ) { this.buf = buf.retain(); }
	
	@Override
	public void handleServerSide( MessageContext ctx )
	{
		final EntityPlayer player = ctx.getServerHandler().player;
		final PlayerPatch patch = PlayerPatch.get( player );
		final IEquippedItem< ? > equipped = patch.getEquipped( EnumHand.MAIN_HAND );
		equipped.handlePacket( this.buf, player );
		this.buf.release();
	}
}
