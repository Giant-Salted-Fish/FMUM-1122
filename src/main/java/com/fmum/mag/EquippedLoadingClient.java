package com.fmum.mag;

import com.fmum.FMUM;
import com.fmum.ammo.IAmmoType;
import com.fmum.animation.SoundFrame;
import com.fmum.input.IInput;
import com.fmum.input.InputManager;
import com.fmum.input.Inputs;
import com.fmum.item.EquippedWrapper;
import com.fmum.item.IEquippedItem;
import com.fmum.item.IItem;
import com.fmum.network.PacketFullMag;
import com.fmum.network.PacketLoadAmmo;
import com.fmum.network.PacketUnwrapEquipped;
import com.fmum.player.ChatBoxUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.OptionalInt;

@SideOnly( Side.CLIENT )
public class EquippedLoadingClient extends EquippedWrapper
{
	protected final String trigger_key;
	
	protected int tick_left = 0;
	protected int sound_idx;
	
	public EquippedLoadingClient( IEquippedItem wrapped, String trigger_key )
	{
		super( wrapped );
		
		this.trigger_key = trigger_key;
	}
	
	@Override
	public IEquippedItem tickInHand( IItem item, EnumHand hand, EntityPlayer player )
	{
		if ( this.tick_left == 0 )
		{
			// Check if we have next ammo to load.
			final IMag mag = IMag.from( item );
			if ( mag.isFull() )
			{
				final String prompt = I18n.format( "chat.mag.full_ammo" );
				ChatBoxUtil.addFixedPrompt( prompt );
				
				// Because every ammo is corresponding to a load packet, \
				// we do not need to send unwrap packet here.
				return this.wrapped.tickInHand( item, hand, player );
			}
			
			final OptionalInt slot = IAmmoType.lookupValidAmmoSlot(
				player.inventory,
				a -> mag.checkAmmoForLoad( a ).isSuccess(),
				InputManager.getBoolState( Inputs.ALT_AMMO ) ? 1 : 0
			);
			if ( !slot.isPresent() ) {
				return this.wrapped.tickInHand( item, hand, player );
			}
			
			final int ammo_slot = slot.getAsInt();
			final boolean full_mag = this.trigger_key.equals( Inputs.RELOAD );
			if ( full_mag && player.isCreative() )
			{
				FMUM.NET.sendPacketC2S( new PacketFullMag( ammo_slot ) );
				return this.wrapped;
			}
			else
			{
				FMUM.NET.sendPacketC2S( new PacketLoadAmmo( ammo_slot ) );
				
				final MagType type = ( MagType ) item.getType();
				this.tick_left = type.op_load_ammo.tick_count;
				this.sound_idx = 0;
			}
		}
		
		final MagType type = ( MagType ) item.getType();
		final MagOpConfig config = type.op_load_ammo;
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
		final String key = input.getAsBool() ? Inputs.RELOAD : Inputs.LOAD_AMMO;
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
