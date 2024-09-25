package com.fmum.network;

import com.fmum.mag.EquippedLoading;
import com.fmum.mag.EquippedMag;
import com.fmum.player.PlayerPatch;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumHand;
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
			.mapEquipped( ( eq, it ) -> {
				if ( eq instanceof EquippedLoading )
				{
					EquippedLoading last = ( EquippedLoading ) eq;
					while ( last.next instanceof EquippedLoading ) {
						last = ( EquippedLoading ) last.next;
					}
					last.next = new EquippedLoading( last.wrapped, it, this.inv_slot );
					return eq;
				}
				else if ( eq instanceof EquippedMag )
				{
					// Tick now to catch up client progress.
					final EquippedLoading loading = new EquippedLoading( eq, it, this.inv_slot );
					return loading.tickInHand( EnumHand.MAIN_HAND, it, player );
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
