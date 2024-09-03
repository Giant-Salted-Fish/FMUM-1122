package com.fmum.network;

import com.fmum.gunpart.IGunPart;
import com.fmum.item.IItem;
import com.fmum.module.IModifyPreview;
import com.fmum.module.IModule;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;

public class PacketAdjustModule implements IPacket
{
	protected static final byte PAINTJOB = 0;
	protected static final byte STEP = 1;
	protected static final byte OFFSET = 2;
	
	protected byte type;
	protected byte[] location;
	protected int value;
	
	public static PacketAdjustModule ofPaintjobSwitch( byte[] location, int idx ) {
		return new PacketAdjustModule( PAINTJOB, location, idx );
	}
	
	public static PacketAdjustModule ofStepAdjust( byte[] location, int step ) {
		return new PacketAdjustModule( STEP, location, step );
	}
	
	public static PacketAdjustModule ofOffsetAdjust( byte[] location, int offset ) {
		return new PacketAdjustModule( OFFSET, location, offset );
	}
	
	public PacketAdjustModule() { }
	
	protected PacketAdjustModule( byte type, byte[] location, int value )
	{
		this.type = type;
		this.location = location;
		this.value = value;
	}
	
	@Override
	public void toBytes( ByteBuf buf )
	{
		buf.writeByte( this.type );
		buf.writeByte( this.location.length / 2 );
		buf.writeBytes( this.location );
		buf.writeShort( this.value );
	}
	
	@Override
	public void fromBytes( ByteBuf buf )
	{
		this.type = buf.readByte();
		final int half_len = 0xFF & buf.readByte();
		this.location = new byte[ 2 * half_len ];
		buf.readBytes( this.location );
		this.value = 0xFFFF & buf.readShort();
	}
	
	@Override
	public void handleServerSide( MessageContext ctx )
	{
		final EntityPlayerMP player = ctx.getServerHandler().player;
		player.getServerWorld().addScheduledTask( () -> {
			final byte[] loc = this.location;
			final Optional< IModule > module = (
				IItem.ofOrEmpty( player.getHeldItemMainhand() )
				.flatMap( it -> it.lookupCapability( IModule.CAPABILITY ) )
				.flatMap( mod -> IModule.tryGetInstalled( mod, loc, loc.length ) )
			);
			if ( !module.isPresent() ) {
				return;
			}
			
			final IModifyPreview< ? > preview = this._tryApply( module.get() );
			if ( !preview.getApplicationError().isPresent() ) {
				preview.apply();
			}
		} );
	}
	
	protected IModifyPreview< ? > _tryApply( IModule module )
	{
		if ( this.type == PAINTJOB ) {
			return module.trySetPaintjob( this.value );
		}
		
		final IGunPart gun_part = ( IGunPart ) module;
		if ( this.type == STEP )
		{
			final int offset = gun_part.getOffset();
			return gun_part.trySetOffsetAndStep( offset, this.value );
		}
		
		if ( this.type == OFFSET )
		{
			final int step = gun_part.getStep();
			return gun_part.trySetOffsetAndStep( this.value, step );
		}
		
		throw new IllegalStateException( "Unknown adjust type: " + this.type );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public void handleClientSide( MessageContext ctx ) {
		throw new UnsupportedOperationException();
	}
}
