package com.fmum.network;

import com.fmum.ammo.IAmmoType;
import com.fmum.mag.IMag;
import com.fmum.player.PlayerPatch;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;

public class PacketClearMag implements IPacket
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
		player.getServerWorld().addScheduledTask( () -> {
			if ( !player.isCreative() ) {
				return;
			}
			
			final PlayerPatch patch = PlayerPatch.of( player );
			final Optional< IMag > opt = patch.getItemIn( EnumHand.MAIN_HAND ).map( IMag::from );
			if ( !opt.isPresent() ) {
				return;
			}
			
			final IMag mag = opt.get();
			if ( mag.isEmpty() ) {
				return;
			}
			
			
			while ( !mag.isEmpty() )
			{
				final IAmmoType ammo = mag.popAmmo();
				final OptionalInt slot = IAmmoType.lookupValidAmmoSlot( player.inventory, ammo::equals, 0 );
				if ( !slot.isPresent() )
				{
					final ItemStack ammo_stack = ammo.newItemStack( ( short ) 0 );
					player.addItemStackToInventory( ammo_stack );
				}
			}
		} );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public void handleClientSide( MessageContext ctx ) {
		throw new UnsupportedOperationException();
	}
}
