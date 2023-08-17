package com.fmum.client.input;

import com.fmum.common.load.BuildableType;
import com.fmum.common.load.IContentBuildContext;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly( Side.CLIENT )
public class KeyBind extends BuildableType implements IKeyBind
{
	// TODO: maybe add support to parse string key code?
	protected int key_code = Keyboard.KEY_NONE;
	
	protected KeyModifier key_modifier = KeyModifier.NONE;
	
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
	public KeyModifier keyModifier() {
		return this.key_modifier;
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
	public void restoreVanillaKeyBind()
	{
		this.vanilla_key_bind.setKeyModifierAndCode(
			this.key_modifier, this.key_code );
	}
	
	@Override
	public BindingState clearVanillaKeyBind()
	{
		final int prev_key_code = this.key_code;
		final KeyModifier prev_key_modifier = this.key_modifier;
		this.key_code = this.vanilla_key_bind.getKeyCode();
		this.key_modifier = this.vanilla_key_bind.getKeyModifier();
		
		this.vanilla_key_bind.setKeyModifierAndCode(
			KeyModifier.NONE, Keyboard.KEY_NONE );
		
		final boolean is_binding_changed = (
			this.key_code != prev_key_code
			|| this.key_modifier != prev_key_modifier
		);
		return is_binding_changed ? BindingState.CHANGED : BindingState.UNCHANGED;
	}
	
	protected void _onPress() {
	
	}
	
	protected void _onRelease() {
	
	}
	
	@Override
	protected String _typeHint() {
		return "KEY_BIND";
	}
}
