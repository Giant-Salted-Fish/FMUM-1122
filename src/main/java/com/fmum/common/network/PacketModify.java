package com.fmum.common.network;

import java.util.function.Consumer;

import com.fmum.common.ModConfig;
import com.fmum.common.item.IItem;
import com.fmum.common.item.IItemTypeHost;
import com.fmum.common.module.IModule;
import com.fmum.common.module.IPreviewPredicate;
import com.fmum.common.paintjob.IPaintable;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class PacketModify implements IPacket
{
	public enum Code
	{
		INSTALL_MODULE
		{
			@Override
			protected void handle( PacketModify packet, EntityPlayer player )
			{
				final InventoryPlayer inv = player.inventory;
				final ItemStack stack = inv.getCurrentItem();
				this.with( stack, wrapper -> {
				
				final byte[] loc = packet.loc;
				final int len = loc.length;
				final int invSlot = loc[ len - 1 ];
				final ItemStack tarStack = inv.getStackInSlot( invSlot );
				this.with( tarStack, module -> {

				// Backup the original stack as the installation test could fail.
				final ItemStack copiedStack = stack.copy();
				final ItemStack copiedTarStack = tarStack.copy();
				final Runnable errorFallback = () -> {
					inv.setInventorySlotContents( inv.currentItem, copiedStack );
					inv.setInventorySlotContents( invSlot, copiedTarStack );
				};
				
				final IModule< ? > base = wrapper.getInstalled( loc, len - 2 );
				final int slot = 0xFF & loc[ len - 2 ];
				final IPreviewPredicate predicate = base.tryInstall( slot, module );
				if( !predicate.ok() )
				{
					// TODO: log error
					errorFallback.run();
					return;
				}
				
				loc[ len - 1 ] = ( byte ) predicate.index();
				wrapper.getInstalled( loc, len ).setOffsetStep( packet.offset(), packet.step() );
				if( !wrapper.checkHitboxConflict( wrapper.getInstalled( loc, len ) ).ok() )
				{
					// TODO: log error
					errorFallback.run();
					return;
				}
				
				// Installed successfully! Remove it from the inventory.
				inv.setInventorySlotContents( invSlot, ItemStack.EMPTY );
				
				} );
				
				} );
			}
		},
		REMOVE_MODULE
		{
			@Override
			protected void handle( PacketModify packet, EntityPlayer player )
			{
				this.with( player.inventory.getCurrentItem(), wrapper -> {
					final byte[] loc = packet.loc;
					final int len = loc.length;
					final IModule< ? > base = wrapper.getInstalled( loc, len - 2 );
					
					final int slot = 0xFF & loc[ len - 2 ];
					final int idx  = 0xFF & loc[ len - 1 ];
					final IModule< ? > removed = base.doRemove( slot, idx );
					player.addItemStackToInventory( removed.toStack() );
				} );
			}
		},
		UPDATE_OFFSET_STEP
		{
			@Override
			protected void handle( PacketModify packet, EntityPlayer player )
			{
				final InventoryPlayer inv = player.inventory;
				final ItemStack stack = inv.getCurrentItem();
				this.with( stack, wrapper -> {
					final byte[] loc = packet.loc;
					final int len = loc.length;
					final IModule< ? > module = wrapper.getInstalled( loc, len );
					
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
			protected void handle( PacketModify packet, EntityPlayer player )
			{
				final InventoryPlayer inv = player.inventory;
				final ItemStack stack = inv.getCurrentItem();
				this.with( stack, wrapper -> {
					final byte[] loc = packet.loc;
					final int len = loc.length;
					
					final IModule< ? > module = wrapper.getInstalled( loc, len );
					final boolean isPaintable = module instanceof IPaintable;
					if( !isPaintable )
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
		
		protected abstract void handle( PacketModify packet, EntityPlayer player );
		
		protected void with( ItemStack stack, Consumer< IModule< ? > > action )
		{
			final IItem item = IItemTypeHost.getItemOrDefault( stack );
			if ( item instanceof IModule< ? > ) { action.accept( ( IModule< ? > ) item ); }
			else { } // TODO: error log
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
