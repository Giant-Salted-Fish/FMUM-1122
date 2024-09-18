package com.fmum.network;

import com.fmum.ammo.IAmmoType;
import com.fmum.item.IItem;
import com.fmum.mag.IMag;
import com.fmum.player.PlayerPatch;
import gsf.util.lang.Result;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;
import java.util.function.IntSupplier;
import java.util.stream.IntStream;

public class PacketFullMag implements IPacket
{
	protected int ammo_slot;
	
	public PacketFullMag() { }
	
	public PacketFullMag( int ammo_slot ) {
		this.ammo_slot = ammo_slot;
	}
	
	@Override
	public void toBytes( ByteBuf buf ) {
		buf.writeByte( this.ammo_slot );
	}
	
	@Override
	public void fromBytes( ByteBuf buf ) {
		this.ammo_slot = 0xFF & buf.readByte();
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
			final Optional< IMag > mag = patch.getItemIn( EnumHand.MAIN_HAND ).map( IMag::from );
			if ( !mag.isPresent() ) {
				return;
			}
			
			final ItemStack as = player.inventory.getStackInSlot( this.ammo_slot );
			final Optional< IAmmoType > ammo = (
				IItem.ofOrEmpty( as )
				.map( IItem::getType )
				.filter( IAmmoType.class::isInstance )
				.map( IAmmoType.class::cast )
			);
			if ( !ammo.isPresent() ) {
				return;
			}
			
			final IMag m = mag.get();
			final IAmmoType a = ammo.get();
			final int cnt = m.getCapacity() - m.getAmmoCount();
			IntStream.range( 0, cnt )
				.mapToObj( i -> m.checkAmmoForLoad( a ) )
				.filter( Result::isSuccess )
				.map( Result::unwrap )
				.forEach( IntSupplier::getAsInt );
		} );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public void handleClientSide( MessageContext ctx ) {
		throw new UnsupportedOperationException();
	}
}
