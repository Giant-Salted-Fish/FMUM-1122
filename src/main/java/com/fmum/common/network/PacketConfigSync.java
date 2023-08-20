package com.fmum.common.network;

import com.fmum.common.ModConfig;
import io.netty.buffer.ByteBuf;

public final class PacketConfigSync implements Packet
{
	@Override
	public void toBytes( ByteBuf buf )
	{
		buf.writeByte( ModConfig.max_modify_layers );
		buf.writeByte( ModConfig.max_slot_capacity );

		buf.writeFloat( ModConfig.cam_drop_cycle );
		buf.writeFloat( ModConfig.cam_drop_ampl );
		buf.writeFloat( ModConfig.cam_drop_impact );
		buf.writeFloat( ModConfig.free_view_limit );
	}
	
	@Override
	public void fromBytes( ByteBuf buf )
	{
		// TODO: also server side?
//		FMUMClient.modifyLoc = new byte[ 2 * ( 0xFF & buf.readByte() ) ];
//		FMUM.maxSlotCapacity = 0xFF & buf.readByte();
//
//		// Camera settings.
//		FMUMClient.camDropCycle  = buf.readFloat() * Util.PI * 0.3F;
//		FMUMClient.camDropAmpl   = buf.readFloat() * 3F;
//		FMUMClient.camDropImpact = buf.readFloat() * 7.5F;
//
//		final float f = buf.readFloat();
//		FMUMClient.freeViewLimitSquared = f * f;
	}
}
