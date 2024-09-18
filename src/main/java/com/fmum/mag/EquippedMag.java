package com.fmum.mag;

import com.fmum.gunpart.EquippedGunPart;
import com.fmum.input.IInput;
import com.fmum.input.Inputs;
import com.fmum.item.IEquippedItem;
import com.fmum.item.IItem;

public class EquippedMag extends EquippedGunPart
{
	@Override
	public IEquippedItem onInputUpdate( String name, IInput input, IItem item )
	{
		if ( input.getAsBool() )
		{
			switch ( name )
			{
			case Inputs.LOAD_AMMO:
			case Inputs.RELOAD:
				return new EquippedLoadingClient( this, name );
			case Inputs.UNLOAD_AMMO:
			case Inputs.LOAD_OR_UNLOAD_MAG:
				return new EquippedUnloadingClient( this, name );
			}
		}
		return super.onInputUpdate( name, input, item );
	}
	
	
}
