package com.fmum.network;

import com.fmum.ModConfig;
import com.fmum.item.IItem;
import com.fmum.module.IModifyPreview;
import com.fmum.module.IModule;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;

public class PacketInstallModule implements IPacket
{
	protected byte[] location;
	protected int inv_slot;
	
	public PacketInstallModule() { }
	
	public PacketInstallModule( byte[] location, int inv_slot )
	{
		this.location = location;
		this.inv_slot = inv_slot;
	}
	
	@Override
	public void toBytes( ByteBuf buf )
	{
		buf.writeByte( this.location.length / 2 );
		buf.writeBytes( this.location );
		buf.writeByte( this.inv_slot );
	}
	
	@Override
	public void fromBytes( ByteBuf buf )
	{
		final int half_len = 0xFF & buf.readByte();
		this.location = new byte[ 1 + 2 * half_len ];
		buf.readBytes( this.location );
		this.inv_slot = 0xFF & buf.readByte();
	}
	
	@Override
	public void handleServerSide( MessageContext ctx )
	{
		if ( this.location.length >= 2 * ModConfig.max_module_depth )
		{
			// TODO: Log error.
			return;
		}
		
		final EntityPlayerMP player = ctx.getServerHandler().player;
		player.getServerWorld().addScheduledTask( () -> {
			final byte[] loc = this.location;
			final Optional< IModule > opt_base = (
				IItem.ofOrEmpty( player.getHeldItemMainhand() )
				.flatMap( it -> it.lookupCapability( IModule.CAPABILITY ) )
				.flatMap( mod -> IModule.tryGetInstalled( mod, loc, loc.length - 1 ) )
			);
			if ( !opt_base.isPresent() ) {
				return;
			}
			
			final Optional< IModule > opt_mod = (
				IItem.ofOrEmpty( player.inventory.getStackInSlot( this.inv_slot ) )
				.flatMap( it -> it.lookupCapability( IModule.CAPABILITY ) )
			);
			if ( !opt_mod.isPresent() ) {
				return;
			}
			
			final int slot_idx = 0xFF & loc[ loc.length - 1 ];
			final IModifyPreview< Integer > preview = opt_base.get().tryInstall( slot_idx, opt_mod.get() );
			if ( preview.getApplicationError().isPresent() ) {
				return;
			}
			
			preview.apply();
			player.inventory.removeStackFromSlot( this.inv_slot );
		} );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public void handleClientSide( MessageContext ctx ) {
		throw new UnsupportedOperationException();
	}
}
