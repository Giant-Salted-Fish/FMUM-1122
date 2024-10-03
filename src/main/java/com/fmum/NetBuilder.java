package com.fmum;

import com.fmum.network.IPacket;
import com.fmum.network.PacketAdjustModule;
import com.fmum.network.PacketClearMag;
import com.fmum.network.PacketFullMag;
import com.fmum.network.PacketInstallModule;
import com.fmum.network.PacketLoadAmmo;
import com.fmum.network.PacketLoadMag;
import com.fmum.network.PacketRemoveModule;
import com.fmum.network.PacketSyncConfig;
import com.fmum.network.PacketUnloadAmmo;
import com.fmum.network.PacketUnloadMag;
import com.fmum.network.PacketUnwrapEquipped;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

final class NetBuilder
{
	private final SimpleNetworkWrapper wrapped;
	
	/**
	 * Count for registered packets to help assign discriminator.
	 */
	private int discriminator = 0;
	
	NetBuilder( String channel ) {
		this.wrapped = NetworkRegistry.INSTANCE.newSimpleChannel( channel );
	}
	
	NetBuilder _regisPackets()
	{
		this.__regisServerPacket( PacketInstallModule.class );
		this.__regisServerPacket( PacketRemoveModule.class );
		this.__regisServerPacket( PacketAdjustModule.class );
		this.__regisServerPacket( PacketUnwrapEquipped.class );
		this.__regisServerPacket( PacketLoadAmmo.class );
		this.__regisServerPacket( PacketUnloadAmmo.class );
		this.__regisServerPacket( PacketFullMag.class );
		this.__regisServerPacket( PacketClearMag.class );
		this.__regisServerPacket( PacketLoadMag.class );
		this.__regisServerPacket( PacketUnloadMag.class );
		
		this.__regisClientPacket( PacketSyncConfig.class );
		return this;
	}
	
	private void __regisServerPacket( Class< ? extends IPacket > packet_class )
	{
		this.wrapped.registerMessage(
			( packet, ctx ) -> {
				packet.handleServerSide( ctx );
				return null;
			},
			packet_class,
			this.discriminator,
			Side.SERVER
		);
		
		this.discriminator += 1;
	}
	
	private void __regisClientPacket( Class< ? extends IPacket > packet_class )
	{
		this.wrapped.registerMessage(
			( packet, ctx ) -> {
				packet.handleClientSide( ctx );
				return null;
			},
			packet_class,
			this.discriminator,
			Side.CLIENT
		);
		
		this.discriminator += 1;
	}
	
	SimpleNetworkWrapper _build() {
		return this.wrapped;
	}
}
