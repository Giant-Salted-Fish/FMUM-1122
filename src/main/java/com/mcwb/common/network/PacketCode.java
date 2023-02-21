package com.mcwb.common.network;

import com.mcwb.common.gun.IGun;
import com.mcwb.common.gun.IMag;
import com.mcwb.common.item.IItem;
import com.mcwb.common.item.IItemTypeHost;
import com.mcwb.common.player.OpUnloadAmmo;
import com.mcwb.common.player.OpUnloadMag;
import com.mcwb.common.player.PlayerPatch;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class PacketCode implements IPacket
{
	public static enum Code
	{
		TERMINATE_OP()
		{
			@Override
			protected void handle( EntityPlayerMP player )
			{
				PlayerPatch.get( player ).ternimateOperating();
				// TODO: notify other players to stop animation
			}
		},
		UNLOAD_AMMO()
		{
			@Override
			protected void handle( EntityPlayerMP player )
			{
				final ItemStack stack = player.inventory.getCurrentItem();
				final IItem item = IItemTypeHost.getType( stack ).getContexted( stack );
				if( !( item instanceof IMag ) ) return;
				
				PlayerPatch.get( player ).tryLaunch( new OpUnloadAmmo( player, ( IMag ) item ) );
			}
		},
		UNLOAD_MAG()
		{
			@Override
			protected void handle( EntityPlayerMP player )
			{
				final ItemStack stack = player.inventory.getCurrentItem();
				final IItem item = IItemTypeHost.getType( stack ).getContexted( stack );
				if( !( item instanceof IGun ) ) return;
				
				PlayerPatch.get( player ).tryLaunch( new OpUnloadMag( player, ( IGun ) item ) );
			}
		},
		
		SWAP_HAND()
		{
			@Override
			protected void handle( EntityPlayerMP player ) {
				PlayerPatch.get( player ).trySwapHand();
			}
		};
		
		protected abstract void handle( EntityPlayerMP player );
	}
	
	protected Code code;
	
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
