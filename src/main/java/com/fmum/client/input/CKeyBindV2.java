package com.fmum.client.input;

import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.input.Keyboard;

public class CKeyBindV2 implements IKeyBind
{
	protected int key_code = Keyboard.KEY_NONE;
	
	protected transient boolean is_down = false;
	
	public String dependOn();
	
	@Override
	public boolean isDown() {
		return this.is_down;
	}
	
	@Override
	public int keyCode() {
		return this.key_code;
	}
	
	@Override
	public KeyModifier keyModifier() {
		return KeyModifier.NONE;
	}
	
	@Override
	public void activeUpdate( boolean is_down )
	{
		if ( this.is_down != is_down )
		{
			this.is_down = is_down;
			final Runnable callback =
				is_down ? this::_onPress : this::_onRelease;
			callback.run();
		}
	}
	
	@Override
	public void inactiveUpdate( boolean is_down )
	{
		if ( !is_down && this.is_down )
		{
			this.is_down = false;
			this._onRelease();
		}
	}
	
	@Override
	public void restoreVanillaKeyBind() { }
	
	@Override
	public BindingState clearVanillaKeyBind() {
		return BindingState.UNCHANGED;
	}
	
	protected void _onPress() {
	
	}
	
	protected void _onRelease() {
	
	}
}
