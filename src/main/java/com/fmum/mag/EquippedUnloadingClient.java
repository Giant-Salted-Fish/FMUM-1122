package com.fmum.mag;

import com.fmum.FMUM;
import com.fmum.animation.SoundFrame;
import com.fmum.input.IInput;
import com.fmum.input.Inputs;
import com.fmum.item.EquippedWrapper;
import com.fmum.item.IEquippedItem;
import com.fmum.item.IItem;
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
	
	protected int tick_left = 0;
	protected int sound_idx;
	protected boolean is_packet_sent = false;
	
	public EquippedUnloadingClient( IEquippedItem wrapped, String trigger_key )
	{
		super( wrapped );
		
		this.trigger_key = trigger_key;
	}
	
	@Override
	public IEquippedItem tickInHand( IItem item, EnumHand hand, EntityPlayer player )
	{
		if ( this.tick_left == 0 )
		{
			final IMag mag = IMag.from( item );
			if ( mag.isEmpty() )
			{
				final String prompt = I18n.format( "chat.mag.no_ammo" );
				ChatBoxUtil.addFixedPrompt( prompt );
				
				// Server side will know that this mag is empty. So no need \
				// to send unwrap packet here.
				return this.wrapped.tickInHand( item, hand, player );
			}
			else if ( !this.is_packet_sent )
			{
				final boolean clear_mag = this.trigger_key.equals( Inputs.LOAD_OR_UNLOAD_MAG );
				if ( clear_mag && player.isCreative() )
				{
					FMUM.NET.sendPacketC2S( new PacketClearMag() );
					return this.wrapped;
				}
				else
				{
					FMUM.NET.sendPacketC2S( new PacketUnloadAmmo() );
					this.is_packet_sent = true;
				}
			}
			
			final MagType type = ( MagType ) item.getType();
			this.tick_left = type.op_unload_ammo.tick_count;
			this.sound_idx = 0;
		}
		
		final MagType type = ( MagType ) item.getType();
		final MagOpConfig config = type.op_unload_ammo;
		final int tick_left = this.tick_left - 1;
		final float progress = 1.0F - ( float ) tick_left / config.tick_count;
		this.sound_idx = SoundFrame.playSound( config.sound_frame, this.sound_idx, progress, player );
		
		this.tick_left = tick_left;
		return this;
	}
	
	@Override
	public IEquippedItem onInputUpdate( IItem item, String name, IInput input )
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
