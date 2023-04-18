package com.mcwb.common.network;

import com.mcwb.common.gun.IEquippedGun;
import com.mcwb.common.gun.IEquippedMag;
import com.mcwb.common.item.IEquippedItem;
import com.mcwb.common.player.OpLoadAmmo;
import com.mcwb.common.player.PlayerPatch;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class PacketCodeAssist implements IPacket
{
	public static enum Code
	{
		LOAD_AMMO
		{
			// TODO: check assist maybe?
			@Override
			protected void handle( PacketCodeAssist packet, EntityPlayerMP player )
			{
				final PlayerPatch patch = PlayerPatch.get( player );
				final IEquippedItem< ? > equipped = patch.getEquipped( EnumHand.MAIN_HAND );
				final boolean isMag = equipped instanceof IEquippedMag< ? >;
				if ( !isMag ) { return; }
				
//				patch.tryLaunch( new OpLoadAmmo( ( IEquippedMag< ? > ) equipped, packet.assist ) );
			}
		},
		LOAD_MAG
		{
			@Override
			protected void handle( PacketCodeAssist packet, EntityPlayerMP player )
			{
				final PlayerPatch patch = PlayerPatch.get( player );
				final IEquippedItem< ? > equipped = patch.getEquipped( EnumHand.MAIN_HAND );
				final boolean isGun = equipped instanceof IEquippedGun< ? >;
				if ( !isGun ) { return; }
				
//				patch.tryLaunch( new OpLoadMag( ( IEquippedGun< ? > ) equipped, packet.assist ) );
			}
		};
		
		protected abstract void handle( PacketCodeAssist packet, EntityPlayerMP player );
	}
	
	protected Code code;
	protected int assist;
	
	public PacketCodeAssist() { }
	
	public PacketCodeAssist( Code code, int assist )
	{
		this.code = code;
		this.assist = assist;
	}
	
	@Override
	public void toBytes( ByteBuf buf )
	{
		buf.writeByte( this.code.ordinal() );
		buf.writeInt( this.assist );
	}
	
	@Override
	public void fromBytes( ByteBuf buf )
	{
		this.code = Code.values()[ buf.readByte() ];
		this.assist = buf.readInt();
	}
	
	@Override
	public void handleServerSide( MessageContext ctx )
	{
		final EntityPlayerMP player = ctx.getServerHandler().player;
		player.getServerWorld().addScheduledTask( () -> this.code.handle( this, player ) );
	}
}
