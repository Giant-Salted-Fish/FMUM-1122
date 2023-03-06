package com.mcwb.common.network;

import java.util.function.Consumer;

import com.mcwb.common.ModConfig;
import com.mcwb.common.item.IItem;
import com.mcwb.common.item.IItemTypeHost;
import com.mcwb.common.module.IModular;
import com.mcwb.common.module.IPreviewPredicate;
import com.mcwb.common.paintjob.IPaintable;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class PacketModify implements IPacket
{
	public static enum Code
	{
		INSTALL_MODULE
		{
			@Override
			protected void handle( PacketModify pkt, EntityPlayerMP player )
			{
				final InventoryPlayer inv = player.inventory;
				final ItemStack stack = inv.getCurrentItem();
				this.withPrimary( stack, wrapper -> {
					
				final byte[] loc = pkt.loc;
				final int len = loc.length;
				final int invSlot = loc[ len - 1 ];
				final ItemStack tarStack = inv.getStackInSlot( invSlot );
				this.withPrimary( tarStack, module -> {
					
				// Backup the original stack as the installation test could fail
				final ItemStack copiedStack = stack.copy();
				final ItemStack copiedTarStack = tarStack.copy();
				
				switch( 0 )
				{
				default:
					final IModular< ? > base = wrapper.getInstalled( loc, len - 2 );
					final int slot = 0xFF & loc[ len - 2 ];
					final IPreviewPredicate predicate = base.tryInstall( slot, module );
					if( !predicate.ok() )
					{
						// TODO: log error
						break;
					}
					
					loc[ len - 1 ] = ( byte ) predicate.index();
					wrapper.getInstalled( loc, len ).setOffsetStep( pkt.offset(), pkt.step() );
					if( !wrapper.checkHitboxConflict( wrapper.getInstalled( loc, len ) ).ok() )
					{
						// TODO: log error
						break;
					}
					
					inv.setInventorySlotContents( invSlot, ItemStack.EMPTY );
					return;
				}
				
				// Installation test failed, restore those stacks
				inv.setInventorySlotContents( inv.currentItem, copiedStack );
				inv.setInventorySlotContents( invSlot, copiedTarStack );
			} ); } ); }
		},
		REMOVE_MODULE
		{
			@Override
			protected void handle( PacketModify packet, EntityPlayerMP player )
			{
				this.withPrimary( player.inventory.getCurrentItem(), wrapper -> {
					final byte[] loc = packet.loc;
					final int len = loc.length;
					final IModular< ? > removed = wrapper.getInstalled( loc, len - 2 )
						.doRemove( 0xFF & loc[ len - 2 ], 0xFF & loc[ len - 1 ] );
					player.addItemStackToInventory( removed.toStack() );
				} );
			}
		},
		UPDATE_OFFSET_STEP
		{
			@Override
			protected void handle( PacketModify packet, EntityPlayerMP player )
			{
				// Set offset and step could fail so backup before use
				final InventoryPlayer inv = player.inventory;
				final ItemStack stack = inv.getCurrentItem();
				
				this.withPrimary( stack, wrapper -> {
					final byte[] loc = packet.loc;
					final int len = loc.length;
					final IModular< ? > module = wrapper.getInstalled( loc, len );
					
					final ItemStack copiedStack = stack.copy();
					module.setOffsetStep( packet.offset(), packet.step() );
					if( !wrapper.checkHitboxConflict( wrapper.getInstalled( loc, len ) ).ok() )
					{
						// TODO: log error
						inv.setInventorySlotContents( inv.currentItem, copiedStack );
					}
				} );
			}
		},
		UPDATE_PAINTJOB
		{
			@Override
			protected void handle( PacketModify packet, EntityPlayerMP player )
			{
				final InventoryPlayer inv = player.inventory;
				final ItemStack stack = inv.getCurrentItem();
				this.withPrimary( stack, wrapper -> {
					final byte[] loc = packet.loc;
					final int len = loc.length;
					
					final IModular< ? > module = wrapper.getInstalled( loc, len );
					if( !( module instanceof IPaintable ) )
					{
						// TODO: log error
						return;
					}
					
					final IPaintable paintable = ( IPaintable ) module;
					if( packet.assist >= paintable.paintjobCount() )
					{
						// TODO: log error
						return;
					}
					
					// TODO: check if player can offer this paintjob
					if( !paintable.tryOffer( packet.assist, player ) )
					{
						// TODO: log error
						return;
					}
					
					final ItemStack copiedStack = stack.copy();
					paintable.setPaintjob( packet.assist );
					if( !wrapper.checkHitboxConflict( wrapper.getInstalled( loc, len ) ).ok() )
					{
						// TODO: log error
						inv.setInventorySlotContents( inv.currentItem, copiedStack );
					}
				} );
			}
		};
		
		protected abstract void handle( PacketModify packet, EntityPlayerMP player );
		
		protected final void withPrimary( ItemStack stack, Consumer< IModular< ? > > action )
		{
			final IItem item = IItemTypeHost.getTypeA( stack ).getContexted( stack );
			if( item instanceof IModular< ? > ) action.accept( ( IModular< ? > ) item );
			else ; // TODO: log error
		}
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
	public PacketModify( int invSlot, int offset, int step, byte[] loc, int len )
	{
		this( Code.INSTALL_MODULE, step << 16 | offset & 0xFFFF, loc, len );
		
		this.loc[ len - 1 ] = ( byte ) invSlot;
	}
	
	@SideOnly( Side.CLIENT )
	public PacketModify( byte[] loc, int len ) { this( Code.REMOVE_MODULE, 0, loc, len ); }
	
	@SideOnly( Side.CLIENT )
	public PacketModify( int offset, int step, byte[] loc, int len ) {
		this( Code.UPDATE_OFFSET_STEP, step << 16 | offset & 0xFFFF, loc, len );
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
