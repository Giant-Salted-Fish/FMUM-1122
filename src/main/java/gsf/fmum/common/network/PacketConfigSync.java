package gsf.fmum.common.network;

import gsf.fmum.client.FMUMClient;
import gsf.fmum.common.FMUM;
import gsf.fmum.common.ModConfig;
import gsf.fmum.util.MathUtil;
import io.netty.buffer.ByteBuf;

public final class PacketConfigSync implements IPacket
{
	@Override
	public void toBytes( ByteBuf buf )
	{
//		buf.writeByte( ModConfig.max_modify_layers );
		buf.writeByte( ModConfig.max_slot_capacity );
		
		buf.writeFloat( ModConfig.camera_drop_cycle );
		buf.writeFloat( ModConfig.camera_drop_amplitude );
		buf.writeFloat( ModConfig.camera_drop_impact );
		buf.writeFloat( ModConfig.free_view_limit );
	}
	
	@Override
	public void fromBytes( ByteBuf buf )
	{
		// TODO: also server side?
//		FMUMClient.modifyLoc = new byte[ 2 * ( 0xFF & buf.readByte() ) ];
		FMUM.max_slot_capacity = 0xFF & buf.readByte();
		
		// Camera settings.
		FMUMClient.camera_drop_cycle = buf.readFloat() * MathUtil.PI * 0.3F;
		FMUMClient.camera_drop_amplitude = buf.readFloat() * 3F;
		FMUMClient.camera_drop_impact = buf.readFloat() * 7.5F;
		
		final float f = buf.readFloat();
		FMUMClient.free_view_limit_squared = f * f;
	}
}
