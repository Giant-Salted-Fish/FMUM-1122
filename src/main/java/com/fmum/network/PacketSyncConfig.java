package com.fmum.network;

import com.fmum.ModConfig;
import com.fmum.SyncConfig;
import gsf.util.math.MoreMath;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class PacketSyncConfig implements IPacket
{
	private int max_module_depth;
	private int max_slot_capacity;
	
	private float camera_drop_cycle;
	private float camera_drop_amplitude;
	private float camera_drop_impact;
	private float free_view_limit;
	
	public PacketSyncConfig() { }
	
	public PacketSyncConfig( ModConfig unused )
	{
		this.max_module_depth = ModConfig.max_module_depth;
		this.max_slot_capacity = ModConfig.max_slot_capacity;
		this.camera_drop_cycle = ModConfig.camera_drop_cycle;
		this.camera_drop_amplitude = ModConfig.camera_drop_amplitude;
		this.camera_drop_impact = ModConfig.camera_drop_impact;
		this.free_view_limit = ModConfig.free_view_limit;
	}
	
	@Override
	public void toBytes( ByteBuf buf )
	{
		buf.writeInt( this.max_module_depth );
		buf.writeInt( this.max_slot_capacity );
		buf.writeFloat( this.camera_drop_cycle );
		buf.writeFloat( this.camera_drop_amplitude );
		buf.writeFloat( this.camera_drop_impact );
		buf.writeFloat( this.free_view_limit );
	}
	
	@Override
	public void fromBytes( ByteBuf buf )
	{
		this.max_module_depth = buf.readInt();
		this.max_slot_capacity = buf.readInt();
		this.camera_drop_cycle = buf.readFloat();
		this.camera_drop_amplitude = buf.readFloat();
		this.camera_drop_impact = buf.readFloat();
		this.free_view_limit = buf.readFloat();
	}
	
	@Override
	public void handleServerSide( MessageContext ctx ) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public void handleClientSide( MessageContext ctx )
	{
		SyncConfig.max_module_depth = this.max_module_depth;
		SyncConfig.max_slot_capacity = this.max_slot_capacity;
		
		// Camera settings.
		SyncConfig.camera_drop_cycle = this.camera_drop_cycle * MoreMath.PI * 0.3F;
		SyncConfig.camera_drop_amplitude = this.camera_drop_amplitude * 3.0F;
		SyncConfig.camera_drop_impact = this.camera_drop_impact * 7.5F;
		
		final float f = this.free_view_limit;
		SyncConfig.free_view_limit_squared = f * f;
	}
}
