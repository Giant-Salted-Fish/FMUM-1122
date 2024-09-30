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
	public IEquippedItem onInputUpdate( IItem item, String name, IInput input )
	{
		if ( input.getAsBool() )
		{
			switch ( name )
			{
			case Inputs.LOAD_OR_UNLOAD_MAG:
				return new EquippedLoadingMag( this, item );
			case Inputs.INSPECT_WEAPON:
				return new EquippedInspectClient( this, item );
			}
		}
		return super.onInputUpdate( item, name, input );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public boolean getViewBobbing( IItem item, boolean original ) {
		return false;
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public boolean shouldDisableCrosshair( IItem item ) {
		return true;
	}
}
