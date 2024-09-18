package com.fmum.network;

import com.fmum.mag.EquippedLoading;
import com.fmum.mag.EquippedMag;
import com.fmum.player.PlayerPatch;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketLoadAmmo implements IPacket
{
	protected int inv_slot;
	
	public PacketLoadAmmo() { }
	
	public PacketLoadAmmo( int inv_slot ) {
		this.inv_slot = inv_slot;
	}
	
	@Override
	public void toBytes( ByteBuf buf ) {
		buf.writeByte( this.inv_slot );
	}
	
	@Override
	public void fromBytes( ByteBuf buf ) {
		this.inv_slot = 0xFF & buf.readByte();
	}
	
	@Override
	public void handleServerSide( MessageContext ctx )
	{
		final EntityPlayerMP player = ctx.getServerHandler().player;
		player.getServerWorld().addScheduledTask( () -> PlayerPatch.of( player )
			.mapEquipped( eq -> {
				if ( eq instanceof EquippedMag ) {
					return new EquippedLoading( eq, this.inv_slot );
				}
				else if ( eq instanceof EquippedLoading )
				{
					final EquippedLoading old = ( EquippedLoading ) eq;
					old.next = new EquippedLoading( old.wrapped, this.inv_slot );
					return old;
				}
				else {
					return eq;
				}
			} )
		);
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public void handleClientSide( MessageContext ctx ) {
		throw new UnsupportedOperationException();
	}
}
