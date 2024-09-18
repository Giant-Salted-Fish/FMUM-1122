package com.fmum.mag;

import com.fmum.FMUM;
import com.fmum.input.IInput;
import com.fmum.input.Inputs;
import com.fmum.item.EquippedWrapper;
import com.fmum.item.IEquippedItem;
import com.fmum.item.IItem;
import com.fmum.network.IPacket;
import com.fmum.network.PacketClearMag;
import com.fmum.network.PacketUnloadAmmo;
import com.fmum.network.PacketUnwrapEquipped;
import com.fmum.player.ChatBoxUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class EquippedUnloadingClient extends EquippedWrapper
{
	protected final String trigger_key;
	
	protected float progress = 1.0F;
	protected float prev_progress;
	protected boolean is_packet_sent = false;
	
	public EquippedUnloadingClient( IEquippedItem wrapped, String trigger_key )
	{
		super( wrapped );
		
		this.trigger_key = trigger_key;
	}
	
	@Override
	public IEquippedItem tickInHand( EnumHand hand, IItem item, EntityPlayer player )
	{
		if ( this.progress >= 1.0F )
		{
			final IMag mag = IMag.from( item );
			if ( mag.isEmpty() )
			{
				final String prompt = I18n.format( "chat.mag.no_ammo" );
				ChatBoxUtil.addFixedPrompt( prompt );
				
				// Server side will know that this mag is empty. So no need \
				// to send unwrap packet here.
				return this.wrapped;
			}
			else if ( !this.is_packet_sent )
			{
				final boolean clear_mag = player.isCreative() && this.trigger_key.equals( Inputs.LOAD_OR_UNLOAD_MAG );
				if ( clear_mag )
				{
					FMUM.NET.sendPacketC2S( new PacketClearMag() );
					return this.wrapped;
				}
				else
				{
					FMUM.NET.sendPacketC2S( new PacketUnloadAmmo() );
					this.is_packet_sent = true;
					this.progress = 0.0F;
				}
			}
		}
		
		final MagType type = ( MagType ) item.getType();
		final MagOpConfig config = type.unload_ammo_op;
		final float progress = this.progress + config.progressor;
		// TODO: Handle sound
		
		
		this.prev_progress = this.progress;
		this.progress = Math.min( 1.0F, progress );
		return this;
	}
	
	@Override
	public IEquippedItem onInputUpdate( String name, IInput input, IItem item )
	{
		// Use trigger key to quit.
		final String key = input.getAsBool() ? Inputs.LOAD_OR_UNLOAD_MAG : Inputs.UNLOAD_AMMO;
		final boolean stop = key.equals( name ) && key.equals( this.trigger_key );
		if ( stop )
		{
			FMUM.NET.sendPacketC2S( new PacketUnwrapEquipped() );
			return this.wrapped;
		}
		else {
			return this;
		}
	}
}
