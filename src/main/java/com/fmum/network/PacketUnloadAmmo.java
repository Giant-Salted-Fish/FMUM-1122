package com.fmum.network;

import com.fmum.mag.EquippedMag;
import com.fmum.mag.SEquippedUnload;
import com.fmum.player.PlayerPatch;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketUnloadAmmo implements IPacket
{
	@Override
	public void toBytes( ByteBuf buf ) {
		// Pass.
	}
	
	@Override
	public void fromBytes( ByteBuf buf ) {
		// Pass.
	}
	
	@Override
	public void handleServerSide( MessageContext ctx )
	{
		final EntityPlayerMP player = ctx.getServerHandler().player;
		player.getServerWorld().addScheduledTask( () -> PlayerPatch.of( player )
			.mapEquipped( ( eq, it ) -> {
				if ( eq instanceof EquippedMag )
				{
					// Tick now to catch up client progress.
					final SEquippedUnload unloading = new SEquippedUnload( eq );
					return unloading.tickInHand( it, EnumHand.MAIN_HAND, player );
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
