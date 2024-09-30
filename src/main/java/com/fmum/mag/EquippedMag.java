package com.fmum.mag;

import com.fmum.gunpart.EquippedGunPart;
import com.fmum.input.IInput;
import com.fmum.input.Inputs;
import com.fmum.item.IEquippedItem;
import com.fmum.item.IItem;

public class EquippedMag extends EquippedGunPart
{
	@Override
	public IEquippedItem onInputUpdate( IItem item, String name, IInput input )
	{
		if ( input.getAsBool() )
		{
			switch ( name )
			{
			case Inputs.LOAD_AMMO:
			case Inputs.RELOAD:
				return new CEquippedLoad( this, name );
			case Inputs.UNLOAD_AMMO:
			case Inputs.LOAD_OR_UNLOAD_MAG:
				return new CEquippedUnload( this, name );
			}
		}
		return super.onInputUpdate( item, name, input );
	}
	
	
}
