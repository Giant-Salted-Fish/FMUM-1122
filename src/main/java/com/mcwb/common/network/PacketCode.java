package com.mcwb.common.network;

import com.mcwb.common.gun.IEquippedGun;
import com.mcwb.common.item.IEquippedItem;
import com.mcwb.common.player.OpUnloadMag;
import com.mcwb.common.player.PlayerPatch;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class PacketCode implements IPacket
{
	public static enum Code
	{
		TERMINATE_OP
		{
			@Override
			protected void handle( EntityPlayerMP player )
			{
				PlayerPatch.get( player ).ternimateExecuting();
				// TODO: notify other players to stop animation
			}
		},
		UNLOAD_AMMO
		{
			@Override
			protected void handle( EntityPlayerMP player )
			{
//				final PlayerPatch patch = PlayerPatch.get( player );
//				final IEquippedItem< ? > equipped = patch.getEquipped( EnumHand.MAIN_HAND );
//				if ( equipped instanceof IEquippedMag< ? > ) {
//					patch.tryLaunch( new OpUnloadAmmo( ( IEquippedMag< ? > ) equipped ) );
//				}
			}
		},
		UNLOAD_MAG
		{
			@Override
			protected void handle( EntityPlayerMP player )
			{
				final PlayerPatch patch = PlayerPatch.get( player );
				final IEquippedItem< ? > equipped = patch.getEquipped( EnumHand.MAIN_HAND );
//				if ( equipped instanceof IEquippedGun< ? > ) {
//					patch.tryLaunch( new OpUnloadMag( ( IEquippedGun< ? > ) equipped ) );
//				}
			}
		};
		
		protected abstract void handle( EntityPlayerMP player );
	}
	
	private Code code;
	
	public PacketCode() { }
	
	public PacketCode( Code code ) { this.code = code; }
	
	@Override
	public void toBytes( ByteBuf buf ) { buf.writeByte( this.code.ordinal() ); }
	
	@Override
	public void fromBytes( ByteBuf buf ) { this.code = Code.values()[ buf.readByte() ]; }
	
	@Override
	public void handleServerSide( MessageContext ctx )
	{
		final EntityPlayerMP player = ctx.getServerHandler().player;
		player.getServerWorld().addScheduledTask( () -> this.code.handle( player ) );
	}
}
