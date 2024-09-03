package com.fmum.network;

import com.fmum.item.IItem;
import com.fmum.module.IModifyPreview;
import com.fmum.module.IModule;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;

public class PacketRemoveModule implements IPacket
{
	protected byte[] location;
	
	public PacketRemoveModule() { }
	
	public PacketRemoveModule( byte[] location ) {
		this.location = location;
	}
	
	@Override
	public void toBytes( ByteBuf buf )
	{
		buf.writeByte( this.location.length / 2 );
		buf.writeBytes( this.location );
	}
	
	@Override
	public void fromBytes( ByteBuf buf )
	{
		final int half_len = 0xFF & buf.readByte();
		this.location = new byte[ 2 * half_len ];
		buf.readBytes( this.location );
	}
	
	@Override
	public void handleServerSide( MessageContext ctx )
	{
		final EntityPlayerMP player = ctx.getServerHandler().player;
		player.getServerWorld().addScheduledTask( () -> {
			final byte[] loc = this.location;
			final Optional< IModule > base = (
				IItem.ofOrEmpty( player.getHeldItemMainhand() )
				.flatMap( it -> it.lookupCapability( IModule.CAPABILITY ) )
				.flatMap( mod -> IModule.tryGetInstalled( mod, loc, loc.length - 2 ) )
			);
			if ( !base.isPresent() ) {
				return;
			}
			
			final int slot_idx = loc[ loc.length - 2 ];
			final int module_idx = loc[ loc.length - 1 ];
			final IModifyPreview< ? extends IModule > preview = base.get().tryRemove( slot_idx, module_idx );
			if ( preview.getApplicationError().isPresent() ) {
				return;
			}
			
			final IModule removed = preview.apply();
			final ItemStack stack = removed.takeAndToStack();
			final boolean success = player.inventory.addItemStackToInventory( stack );
			if ( !success ) {
				player.dropItem( stack, false );
			}
		} );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public void handleClientSide( MessageContext ctx ) {
		throw new UnsupportedOperationException();
	}
}
