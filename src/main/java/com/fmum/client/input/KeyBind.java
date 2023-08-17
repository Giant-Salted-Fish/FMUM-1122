package com.fmum.client.input;

import com.google.gson.annotations.SerializedName;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly( Side.CLIENT )
public class KeyBind extends BuildableType implements IKeyBind
{
	// TODO: maybe add support to parse string key code?
	protected int key_code = Keyboard.KEY_NONE;
	
	protected String category;

	protected transient KeyBinding vanilla_key_bind;

	protected transient boolean is_down;

	@Override
	public void buildClientSide( IContentBuildContext ctx )
	{
		super.buildClientSide( ctx );

		IKeyBind.REGISTRY.regis( this );
	}

	@Override
	public String category() {
		return this.category;
	}

	@Override
	public boolean isDown() {
		return this.is_down;
	}

	@Override
	public int keyCode() {
		return this.key_code;
	}

	@Override
	public void activeUpdate( boolean is_down )
	{
		if ( this.is_down != is_down )
		{
			this.is_down = is_down;
			final Runnable callback = 
				is_down ? this::_onPresse : this::_onRelease;
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
	public void restoreVanillaKeyBind() {
		this.vanilla_key_bind.setKeyCode( this.key_code );
	}

	@Override
	public KeyBindState clearVanillaKeyBind()
	{
		final int key_code = this.vanilla_key_bind.getKeyCode();
		this.vanilla_key_bind.setKeyCode( Keyboard.KEY_NONE );
		if ( key_code == this.key_code ) {
			return KeyBindState.BOUNDEN_KEY_UNCHANG;
		}

		this.key_code = key_code;
		return KeyBindState.BOUNDEN_KEY_CHANGED;
	}

	protected void _onPress() {

	}

	protected void _onRelease() {

	}
}
