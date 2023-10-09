package com.fmum.client.input;

import com.fmum.common.load.IContentBuildContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class ToggleKeyBindType extends KeyBindType
{
	@Override
	protected void _createKeyBind( IContentBuildContext ctx )
	{
		final KeyBind kb = new KeyBind()
		{
			private boolean state_flag;
			
			@Override
			protected void _onPress()
			{
				final boolean new_state = !this.state_flag;
				InputManager.emitBoolSignal(
					ToggleKeyBindType.this.signal, new_state );
				this.state_flag = new_state;
			}
			
			@Override
			protected void _onRelease() { }
		};
		ctx.regisPostLoadCallback( kb::_setupCombinations );
	}
}
