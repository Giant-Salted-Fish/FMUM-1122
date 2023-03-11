package com.mcwb.common.network;

import com.mcwb.common.gun.IEquippedGun;
import com.mcwb.common.gun.IEquippedMag;
import com.mcwb.common.gun.IGun;
import com.mcwb.common.item.IEquippedItem;
import com.mcwb.common.item.IItem;
import com.mcwb.common.item.IItemTypeHost;
import com.mcwb.common.player.OpLoadAmmo;
import com.mcwb.common.player.OpLoadMag;
import com.mcwb.common.player.PlayerPatch;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
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
				final IEquippedItem item = patch.getEquipped( EnumHand.MAIN_HAND );
				if( !( item instanceof IEquippedMag ) ) return;
				
				patch.tryLaunch( new OpLoadAmmo( player, ( IEquippedMag ) item, packet.assist ) );
			}
		},
		LOAD_MAG
		{
			@Override
			protected void handle( PacketCodeAssist packet, EntityPlayerMP player )
			{
				final ItemStack stack = player.inventory.getCurrentItem();
				final IItem item = IItemTypeHost.getTypeOrDefault( stack ).getContexted( stack );
				if( !( item instanceof IGun< ? > ) ) return;
				
				PlayerPatch.get( player ).tryLaunch(
					equipped -> new OpLoadMag( player, ( IEquippedGun ) equipped, packet.assist )
				);
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
