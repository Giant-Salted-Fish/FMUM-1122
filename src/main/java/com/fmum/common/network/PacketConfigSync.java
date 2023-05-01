package com.fmum.common.network;

import com.fmum.client.FMUMClient;
import com.fmum.common.FMUM;
import com.fmum.common.ModConfig;
import com.fmum.util.Util;

import io.netty.buffer.ByteBuf;

public final class PacketConfigSync implements IPacket
{
	@Override
	public void toBytes( ByteBuf buf )
	{
		buf.writeByte( ModConfig.maxModifyLayers );
		buf.writeByte( ModConfig.maxSlotCapacity );
		
		buf.writeFloat( ModConfig.camDropCycle );
		buf.writeFloat( ModConfig.camDropAmpl );
		buf.writeFloat( ModConfig.camDropImpact );
		buf.writeFloat( ModConfig.freeViewLimit );
	}
	
	@Override
	public void fromBytes( ByteBuf buf )
	{
		// TODO: also server side?
		FMUMClient.modifyLoc = new byte[ 2 * ( 0xFF & buf.readByte() ) ];
		FMUM.maxSlotCapacity = 0xFF & buf.readByte();
		
		/// *** Camera settings. *** ///
		FMUMClient.camDropCycle  = buf.readFloat() * Util.PI * 0.3F;
		FMUMClient.camDropAmpl   = buf.readFloat() * 3F;
		FMUMClient.camDropImpact = buf.readFloat() * 7.5F;
		
		final float limit = buf.readFloat();
		FMUMClient.freeViewLimitSquared = limit * limit;
	}
}
