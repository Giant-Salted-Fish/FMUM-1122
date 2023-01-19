package com.mcwb.common.network;

import com.mcwb.common.ModConfig;
import com.mcwb.common.item.ItemMeta;
import com.mcwb.common.item.MetaHostItem;
import com.mcwb.common.modify.ModifiableContext;
import com.mcwb.common.modify.ModifiableMeta;
import com.mcwb.common.modify.ModuleSlot;

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
		
		// TODO: maybe surround with try-catch to print error
		final EntityPlayerMP player = ctx.getServerHandler().player;
		player.getServerWorld().addScheduledTask( () -> {
			// Make sure the operation target is still valid when this packet arrives
			final ItemStack stack = player.inventory.getCurrentItem();
			final ItemMeta meta = MetaHostItem.getMeta( stack );
			if( !( meta instanceof ModifiableMeta ) ) return;
			
			final int len = this.loc.length;
			final ModifiableMeta primaryMeta = ( ModifiableMeta ) meta;
			final ModifiableContext primaryCtx = primaryMeta
				.getContext( stack, stack.getTagCompound() );
			switch( this.code )
			{
			case INSTALL_MODULE:
				// Check if item to install is a module
				final ItemStack tarStack = player.inventory.getStackInSlot( this.loc[ len - 1 ] );
				final ItemMeta tarMeta = MetaHostItem.getMeta( stack );
				if( !( tarMeta instanceof ModifiableMeta ) )
				{
					// TODO: log error
					break;
				}
				
				// Validate before actual install
				final ModifiableContext baseCtx = primaryCtx.getInstalled( this.loc, len - 2 );
				final int slot = 0xFF & this.loc[ len - 2 ];
				final ModuleSlot moduelSlot = baseCtx.meta().getSlot( slot );
				final ModifiableMeta tarModule = ( ModifiableMeta ) tarMeta;
				if(
					baseCtx.getInstalledCount( slot )
						>= Math.min( moduelSlot.capacity(), ModConfig.maxSlotCapacity )
					|| !moduelSlot.isAllowed( tarModule )
				) {
					// TODO: log error
					break;
				}
				
				// TODO: check hitbox conflict
				
				final ModifiableContext tarCtx = tarModule
					.getContext( tarStack, tarStack.getTagCompound() );
				tarCtx.$step( this.step() );
				tarCtx.$offset( this.offset() );
				
				baseCtx.install( tarCtx, slot );
				tarStack.shrink( 1 );
				break;
				
			case UNSTALL_MODULE:
				final ModifiableContext unstalled = primaryCtx.getInstalled( this.loc, len - 2 )
					.unstall( 0xFF & this.loc[ len - 2 ], 0xFF & this.loc[ len - 1 ] );
				// TODO: give it back to player
				break;
				
			case UPDATE_STEP_OFFSET:
				
				final ModifiableContext modifiable = primaryCtx.getInstalled( this.loc, len );
				final int step = this.step();
				final int offset = this.offset();
				final ModifiableMeta modMeta = modifiable.meta();
//				if( step > modMeta.s) // TODO: check step and offset
				modifiable.$step( this.step() );
				modifiable.$offset( this.offset() );
				
				// TODO: Check hitbox
				break;
				
			case UPDATE_PAINTJOB:
				final ModifiableContext paintable = primaryCtx.getInstalled( this.loc, len );
				if( this.assist >= paintable.paintjob() )
				{
					// TODO: log error
					break;
				}
				
				// TODO: check if player can offer this paintjob
//				if( player.capabilities.isCreativeMode )
					paintable.$paintjob( this.assist );
				break;
				
			default:
				// TODO: log error
			}
		} );
	}
	
	protected final int step() { return this.assist >>> 16; }
	
	protected final int offset() { return 0xFFFF & this.assist; }
}
