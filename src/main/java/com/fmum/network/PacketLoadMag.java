package com.fmum.network;

import com.fmum.gun.EquippedGun;
import com.fmum.gun.SEquippedLoadMag;
import com.fmum.player.PlayerPatch;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketLoadMag implements IPacket
{
	protected int inv_slot;
	
	public PacketLoadMag() { }
	
	public PacketLoadMag( int inv_slot ) {
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
				if ( eq instanceof EquippedGun )
				{
					final SEquippedLoadMag loading = new SEquippedLoadMag( eq, it, this.inv_slot );
					return loading.tickInHand( it, EnumHand.MAIN_HAND, player );
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
