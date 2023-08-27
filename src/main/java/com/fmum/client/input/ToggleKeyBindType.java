package com.fmum.client.input;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class ToggleKeyBindType extends KeyBindType
{
	@Override
	protected KeyBind _createKeyBind()
	{
		return new KeyBind()
		{
			private boolean state_flag;
			
			@Override
			protected void _onPress()
			{
				this.state_flag = !this.state_flag;
				InputSignal.emitBoolSignal(
					ToggleKeyBindType.this.signal, this.state_flag );
			}
			
			@Override
			protected void _onRelease() {
				this._onPress();
			}
		};
	}
}
