package com.mcwb.common.network;

import com.mcwb.common.gun.IMag;
import com.mcwb.common.item.IItem;
import com.mcwb.common.item.IItemTypeHost;
import com.mcwb.common.player.OpLoadAmmo;
import com.mcwb.common.player.PlayerPatch;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class PacketCodeAssist implements IPacket
{
	public static enum Code
	{
		LOAD_AMMO()
		{
			@Override
			protected void handle( PacketCodeAssist packet, EntityPlayerMP player )
			{
				if( packet.assist < 0 || packet.assist >= player.inventory.getSizeInventory() )
				{
					// TODO: log error
					return;
				}
				
				final ItemStack stack = player.inventory.getCurrentItem();
				final IItem item = IItemTypeHost.getType( stack ).getContexted( stack );
				if( !(item instanceof IMag ) ) return;
				
				final IMag mag = ( IMag ) item;
				PlayerPatch.get( player ).tryLaunch( new OpLoadAmmo( player, mag, packet.assist ) );
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
