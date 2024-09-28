package com.fmum.gun;

import com.fmum.gunpart.EquippedGunPart;
import com.fmum.input.IInput;
import com.fmum.input.Inputs;
import com.fmum.item.IEquippedItem;
import com.fmum.item.IItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EquippedGun extends EquippedGunPart
{
	@Override
	@SideOnly( Side.CLIENT )
	public IEquippedItem onInputUpdate( String name, IInput input, IItem item )
	{
		if ( input.getAsBool() )
		{
			switch ( name )
			{
			case Inputs.LOAD_OR_UNLOAD_MAG:
				return new EquippedLoadingMag( this );
			case Inputs.INSPECT_WEAPON:
				return new EquippedInspectClient( this, item );
			}
		}
		return super.onInputUpdate( name, input, item );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public boolean getViewBobbing( boolean original, IItem item ) {
		return false;
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public boolean shouldDisableCrosshair( IItem item ) {
		return true;
	}
}
