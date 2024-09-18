package com.fmum.mag;

import com.fmum.FMUM;
import com.fmum.ammo.IAmmoType;
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
	
	protected float progress = 1.0F;
	protected float prev_progress;
	protected boolean is_packet_sent = false;
	
	public EquippedLoadingClient( IEquippedItem wrapped, String trigger_key )
	{
		super( wrapped );
		
		this.trigger_key = trigger_key;
	}
	
	@Override
	public IEquippedItem tickInHand( EnumHand hand, IItem item, EntityPlayer player )
	{
		if ( this.progress >= 1.0F )
		{
			// Check if we have next ammo to load.
			final IMag mag = IMag.from( item );
			if ( mag.isFull() )
			{
				final String prompt = I18n.format( "chat.mag.full_ammo" );
				ChatBoxUtil.addFixedPrompt( prompt );
				
				// Because every ammo is corresponding to a load packet, \
				// we do not need to send unwrap packet here.
				return this.wrapped;
			}
			
			final OptionalInt slot = IAmmoType.lookupValidAmmoSlot(
				player.inventory,
				a -> mag.checkAmmoForLoad( a ).isSuccess(),
				InputManager.getBoolState( Inputs.ALT_AMMO ) ? 1 : 0
			);
			if ( slot.isPresent() )
			{
				final int ammo_slot = slot.getAsInt();
				final boolean full_mag = player.isCreative() && this.trigger_key.equals( Inputs.RELOAD );
				if ( full_mag )
				{
					FMUM.NET.sendPacketC2S( new PacketFullMag( ammo_slot ) );
					return this.wrapped;
				}
				else
				{
					FMUM.NET.sendPacketC2S( new PacketLoadAmmo( ammo_slot ) );
					this.is_packet_sent = true;
					this.progress = 0.0F;
				}
			}
			else {
				return this.wrapped;
			}
		}
		
		final MagType type = ( MagType ) item.getType();
		final MagOpConfig config = type.load_ammo_op;
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
