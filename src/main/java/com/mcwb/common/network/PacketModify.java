package com.mcwb.common.network;

import java.util.function.Consumer;

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
		final Consumer< IModifiable > handler =
			INSTALL_MODULE == this.code ? primary -> {
				// Check the meta of item to install
				final InventoryPlayer inv = player.inventory;
				final ItemStack tarStack = inv.getStackInSlot( this.loc[ len - 1] );
				final IItemType tarType = IItemTypeHost.getType( tarStack );
				if( !( tarType instanceof IModifiableType ) )
				{
					// TODO: log error
					return;
				}
				
				// Copy primary and target stack to validate install
				final ItemStack copiedStack = inv.getCurrentItem().copy();
				final IModifiable copiedPrimary = ( IModifiable )
					IItemTypeHost.getType( copiedStack ).getContexted( copiedStack );
				final IModifiable base = copiedPrimary.getInstalled( this.loc, len - 2 );
				
				final ItemStack copiedTarStack = tarStack.copy();
				final IModifiable tarModule = ( ( IModifiableType )
					tarType ).getContexted( copiedTarStack );
				
				final int slot = 0xFF & this.loc[ len - 2 ];
				if( !base.tryInstallPreview( slot, tarModule ).ok() )
				{
					// TODO: log error
					return;
				}
				
				tarModule.$step( this.step() ); // TODO: maybe check step and offset?
				tarModule.$offset( this.offset() );
				
				if( !copiedPrimary.checkInstalledPosition( tarModule ).ok() )
				{
					// TODO: log error
					return;
				}
				
				player.inventory.setItemStack( ItemStack.EMPTY );
				inv.setInventorySlotContents( inv.currentItem, copiedStack );
				tarStack.shrink( 1 );
			} :
			UNSTALL_MODULE == this.code ? primary -> {
				final IModifiable removed = primary.getInstalled( this.loc, len - 2 )
					.remove( 0xFF & this.loc[ len - 2 ], 0xFF & this.loc[ len - 1 ] );
				player.addItemStackToInventory( removed.toStack() );
			} :
			UPDATE_STEP_OFFSET == this.code ? primary -> {
				final IModifiable module = primary.getInstalled( this.loc, len );
				final int step = this.step();
				final int offset = this.offset();
//				if( step > modMeta.s) // TODO: check step and offset
				module.$step( step );
				module.$offset( offset );
				
				// TODO: check hitbox
			} :
			UPDATE_PAINTJOB == this.code ? primary -> {
				final IModifiable paintable = primary.getInstalled( this.loc, len );
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
			final IItemType type = IItemTypeHost.getType( stack );
			// TODO: maybe surround with try-catch to print error
			if( type instanceof IModifiableType )
				handler.accept( ( ( IModifiableType ) type ).getContexted( stack ) );
		} );
	}
	
	protected final int step() { return this.assist >>> 16; }
	
	protected final int offset() { return 0xFFFF & this.assist; }
}
