package com.mcwb.common.network;

import com.mcwb.common.ModConfig;
import com.mcwb.common.item.IItemType;
import com.mcwb.common.item.IItemTypeHost;
import com.mcwb.common.modify.IModifiable;
import com.mcwb.common.modify.IModifiableType;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketModify implements IPacket
{
	public static enum Code
	{
		INSTALL_MODULE()
		{
			@Override
			protected void handle( PacketModify packet, EntityPlayerMP player )
			{
				final InventoryPlayer inv = player.inventory;
				final ItemStack stack = inv.getCurrentItem();
				final IItemType type = IItemTypeHost.getType( stack );
				if( !( type instanceof IModifiableType ) )
				{
					// TODO: log error
					return;
				}
				
				final byte[] loc = packet.loc;
				final int len = loc.length;
				final ItemStack tarStack = inv.getStackInSlot( loc[ len - 1] );
				final IItemType tarType = IItemTypeHost.getType( tarStack );
				if( !( tarType instanceof IModifiableType ) )
				{
					// TODO: log error
					return;
				}
				
				// Backup primary and target stack before install validation so that we can set \
				// them back if installation test has failed
				final ItemStack copiedStack = stack.copy();
				final ItemStack copiedTarStack = tarStack.copy();
				
				final IModifiable primary = ( ( IModifiableType ) type ).getContexted( stack );
				final IModifiable base = primary.getInstalled( loc, len - 2 );
				final IModifiable tarModule = ( ( IModifiableType ) tarType )
					.getContexted( tarStack );
				
				switch( 0 )
				{
				default:
					final int slot = 0xFF & loc[ len - 2 ];
					if( !base.tryInstallPreview( slot, tarModule ).ok() )
					{
						// TODO: log error
						break;
					}
					
					tarModule.$step( packet.step() ); // TODO: maybe check step and offset?
					tarModule.$offset( packet.offset() );
					
					if( !primary.checkInstalledPosition( tarModule ).ok() )
					{
						// TODO: log error
						break;
					}
					
					tarStack.shrink( 1 );
					return;
				}
				
				// Installation test failed, restore those stacks
				inv.setInventorySlotContents( inv.currentItem, copiedStack );
				inv.setInventorySlotContents( loc[ len - 1 ], copiedTarStack );
			}
		},
		UNINSTALL_MODULE()
		{
			@Override
			protected void handle( PacketModify packet, EntityPlayerMP player )
			{
				final ItemStack stack = player.inventory.getCurrentItem();
				final IItemType type = IItemTypeHost.getType( stack );
				if( !( type instanceof IModifiableType ) )
				{
					// TODO: log error
					return;
				}
				final IModifiable primary = ( ( IModifiableType ) type ).getContexted( stack );
				
				final byte[] loc = packet.loc;
				final int len = loc.length;
				final IModifiable removed = primary.getInstalled( loc, len - 2 )
					.remove( 0xFF & loc[ len - 2 ], 0xFF & loc[ len - 1 ] );
				player.addItemStackToInventory( removed.toStack() );
			}
		},
		UPDATE_STEP_OFFSET()
		{
			@Override
			protected void handle( PacketModify packet, EntityPlayerMP player )
			{
				final ItemStack stack = player.inventory.getCurrentItem();
				final IItemType type = IItemTypeHost.getType( stack );
				if( !( type instanceof IModifiableType ) )
				{
					// TODO: log error
					return;
				}
				final IModifiable primary = ( ( IModifiableType ) type ).getContexted( stack );
				
				final IModifiable module = primary.getInstalled( packet.loc, packet.loc.length );
				final int step = packet.step();
				final int offset = packet.offset();
//				if( step > modMeta.s) // TODO: check step and offset
				module.$step( step );
				module.$offset( offset );
				
				// TODO: check hitbox
			}
		},
		UPDATE_PAINTJOB()
		{
			@Override
			protected void handle( PacketModify packet, EntityPlayerMP player )
			{
				final ItemStack stack = player.inventory.getCurrentItem();
				final IItemType type = IItemTypeHost.getType( stack );
				if( !( type instanceof IModifiableType ) )
				{
					// TODO: log error
					return;
				}
				final IModifiable primary = ( ( IModifiableType ) type ).getContexted( stack );
				
				final IModifiable paintable = primary.getInstalled( packet.loc, packet.loc.length );
				if( packet.assist >= paintable.paintjob() )
				{
					// TODO: log error
					return;
				}
				
				// TODO: check if player can offer this paintjob
//				if( player.capabilities.isCreativeMode )
					paintable.$paintjob( packet.assist );
			}
		};
		
		protected abstract void handle( PacketModify packet, EntityPlayerMP player );
	}
	
	protected Code code;
	
	protected int assist;
	
	protected byte[] loc;
	
	public PacketModify() { }
	
	@SideOnly( Side.CLIENT )
	public PacketModify( Code code, int assist, byte[] loc, int len )
	{
		this.code = code;
		this.assist = assist;
		this.loc = new byte[ len ];
		System.arraycopy( loc, 0, this.loc, 0, len );
	}
	
	@SideOnly( Side.CLIENT )
	public PacketModify( int invSlot, int step, int offset, byte[] loc, int len )
	{
		this( Code.INSTALL_MODULE, step << 16 | offset & 0xFFFF, loc, len );
		
		this.loc[ len - 1 ] = ( byte ) invSlot;
	}
	
	@SideOnly( Side.CLIENT )
	public PacketModify( byte[] loc, int len ) { this( Code.UNINSTALL_MODULE, 0, loc, len ); }
	
	@SideOnly( Side.CLIENT )
	public PacketModify( int step, int offset, byte[] loc, int len ) {
		this( Code.UPDATE_STEP_OFFSET, step << 16 | offset & 0xFFFF, loc, len );
	}
	
	@SideOnly( Side.CLIENT )
	public PacketModify( int paintjob, byte[] loc, int len ) {
		this( Code.UPDATE_PAINTJOB, paintjob, loc, len );
	}
	
	@Override
	public void toBytes( ByteBuf buf )
	{
		buf.writeByte( this.code.ordinal() );
		buf.writeInt( this.assist );
		buf.writeShort( this.loc.length / 2 );
		buf.writeBytes( this.loc );
	}
	
	@Override
	public void fromBytes( ByteBuf buf )
	{
		this.code = Code.values()[ buf.readByte() ];
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
		
		final EntityPlayerMP player = ctx.getServerHandler().player;
		player.getServerWorld().addScheduledTask( () -> this.code.handle( this, player ) );
	}
	
	protected final int step() { return this.assist >>> 16; }
	
	protected final int offset() { return 0xFFFF & this.assist; }
}
