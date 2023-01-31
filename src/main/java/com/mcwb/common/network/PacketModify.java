package com.mcwb.common.network;

import java.util.function.Consumer;

import com.mcwb.common.ModConfig;
import com.mcwb.common.item.IItemMeta;
import com.mcwb.common.item.IItemMetaHost;
import com.mcwb.common.modify.IContextedModifiable;
import com.mcwb.common.modify.IModifiableMeta;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketModify implements IPacket
{
	public static final byte
		INSTALL_MODULE = 0,
		UNSTALL_MODULE = 1,
		UPDATE_STEP_OFFSET = 2,
		UPDATE_PAINTJOB = 3;
	
	protected byte code;
	
	protected int assist;
	
	protected byte[] loc;
	
	public PacketModify() { }
	
	@SideOnly( Side.CLIENT )
	public PacketModify( byte code, int assist, byte[] loc, int len )
	{
		this.code = code;
		this.assist = assist;
		this.loc = new byte[ len ];
		System.arraycopy( loc, 0, this.loc, 0, len );
	}
	
	@SideOnly( Side.CLIENT )
	public PacketModify( int invSlot, int step, int offset, byte[] loc, int len )
	{
		this( INSTALL_MODULE, step << 16 | offset & 0xFFFF, loc, len );
		
		this.loc[ len - 1 ] = ( byte ) invSlot;
	}
	
	@SideOnly( Side.CLIENT )
	public PacketModify( byte[] loc, int len ) { this( UNSTALL_MODULE, 0, loc, len ); }
	
	@SideOnly( Side.CLIENT )
	public PacketModify( int step, int offset, byte[] loc, int len ) {
		this( UPDATE_STEP_OFFSET, step << 16 | offset & 0xFFFF, loc, len );
	}
	
	@SideOnly( Side.CLIENT )
	public PacketModify( int paintjob, byte[] loc, int len ) {
		this( UPDATE_PAINTJOB, paintjob, loc, len );
	}
	
	@Override
	public void toBytes( ByteBuf buf )
	{
		buf.writeByte( this.code );
		buf.writeInt( this.assist );
		buf.writeShort( this.loc.length / 2 );
		buf.writeBytes( this.loc );
	}
	
	@Override
	public void fromBytes( ByteBuf buf )
	{
		this.code = buf.readByte();
		this.assist = buf.readInt();
		this.loc = new byte[ buf.readShort() * 2 ];
		buf.readBytes( this.loc );
	}
	
	@Override
	public void handleServerSide( MessageContext ctx )
	{
		// Make sure required operation does not exceed the layer limit
		if( this.loc.length > 2 * ModConfig.maxModifyLayers )
		{
			// TODO: log error
			return;
		}
		
		final int len = this.loc.length;
		final EntityPlayerMP player = ctx.getServerHandler().player;
		final Consumer< IContextedModifiable > handler =
			INSTALL_MODULE == this.code ? primary -> {
				// Check the meta of item to install
				final ItemStack tarStack = player.inventory.getStackInSlot( this.loc[ len - 1] );
				final IItemMeta tarMeta = IItemMetaHost.getMeta( tarStack );
				if( !( tarMeta instanceof IModifiableMeta ) )
				{
					// TODO: log error
					return;
				}
				
				// Validate before actual install
				final IContextedModifiable base = primary.getInstalled( this.loc, len - 2 );
				final IContextedModifiable tarModule = ( ( IModifiableMeta ) tarMeta )
					.getContexted( tarStack );
				final int slot = 0xFF & this.loc[ len - 2 ];
				if( !base.canInstall( slot, tarModule ) )
				{
					// TODO: log error
					return;
				}
				// TODO: check hitbox conflict
				
				tarModule.$step( this.step() ); // TODO: maybe check step and offset?
				tarModule.$offset( this.offset() );
				base.install( slot, tarModule );
			} :
			UNSTALL_MODULE == this.code ? primary -> {
				final IContextedModifiable removed = primary.getInstalled( this.loc, len - 2 )
					.remove( 0xFF & this.loc[ len - 2 ], 0xFF & this.loc[ len - 1 ] );
				// TODO: give it back to player
			} :
			UPDATE_STEP_OFFSET == this.code ? primary -> {
				final IContextedModifiable module = primary.getInstalled( this.loc, len );
				final int step = this.step();
				final int offset = this.offset();
//				if( step > modMeta.s) // TODO: check step and offset
				module.$step( step );
				module.$offset( offset );
				
				// TODO: check hitbox
			} :
			UPDATE_PAINTJOB == this.code ? primary -> {
				final IContextedModifiable paintable = primary.getInstalled( this.loc, len );
				if( this.assist >= paintable.paintjob() )
				{
					// TODO: log error
					return;
				}
				
				// TODO: check if player can offer this paintjob
//				if( player.capabilities.isCreativeMode )
					paintable.$paintjob( this.assist );
			} : null;
		
		player.getServerWorld().addScheduledTask( () -> {
			// Make sure the operation target is still valid when this packet arrives
			final ItemStack stack = player.inventory.getCurrentItem();
			final IItemMeta meta = IItemMetaHost.getMeta( stack );
			// TODO: maybe surround with try-catch to print error
			if( meta instanceof IModifiableMeta )
				handler.accept( ( ( IModifiableMeta ) meta ).getContexted( stack ) );
		} );
	}
	
	protected final int step() { return this.assist >>> 16; }
	
	protected final int offset() { return 0xFFFF & this.assist; }
}
