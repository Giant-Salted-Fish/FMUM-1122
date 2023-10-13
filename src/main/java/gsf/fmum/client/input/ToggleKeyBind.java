package com.fmum.client.input;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class ToggleKeyBind extends KeyBind
{
	protected boolean state_flag;
	
	@Override
	protected void _onPress()
	{
		final boolean new_state = !this.state_flag;
		InputManager.emitBoolSignal(
			ToggleKeyBind.this.signal, new_state );
		this.state_flag = new_state;
	}
	
	@Override
	protected void _onRelease() { }
}
