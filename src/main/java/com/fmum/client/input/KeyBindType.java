package com.fmum.client.input;

import com.fmum.client.FMUMClient;
import com.fmum.common.load.BuildableType;
import com.fmum.common.load.IContentBuildContext;
import com.google.gson.annotations.SerializedName;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.Optional;
import java.util.function.Supplier;

@SideOnly( Side.CLIENT )
public class KeyBindType extends BuildableType
{
	protected static final String
		KEY_BIND_PREFIX = FMUMClient.MODID + ".key.",
		KEY_CATEGORY_PREFIX = FMUMClient.MODID + ".key_category.";
	
	protected String category = KEY_CATEGORY_PREFIX + "common";
	
	protected String signal;
	
	protected String depend_on_signal = "";
	
	@SerializedName( value = "default_key_code", alternate = "key_code" )
	protected int default_key_code = Keyboard.KEY_NONE;
	
	@SerializedName( value = "default_key_modifier", alternate = "key_modifier" )
	protected KeyModifier default_key_modifier = KeyModifier.NONE;
	
	protected IKeyConflictContext conflict_context = KeyConflictContext.IN_GAME;
	
	@Override
	public void buildClientSide( IContentBuildContext ctx )
	{
		super.buildClientSide( ctx );
		
		this.signal = Optional.ofNullable( this.signal ).orElse( this.name );
		this._createKeyBind();
	}
	
	protected void _createKeyBind() {
		new KeyBind();
	}
	
	@Override
	protected String _typeHint() {
		return "KEY_BIND";
	}
	
	public class KeyBind implements IKeyBind
	{
		protected final KeyBinding vanilla_key_bind;
		
		protected int key_code;
		
		protected KeyModifier key_modifier;
		
		protected Supplier< Boolean > active_condition;
		
		protected boolean is_down;
		
		protected KeyBind()
		{
			IKeyBind.REGISTRY.regis( this );
			
			this.vanilla_key_bind = new KeyBinding(
				KeyBindType.this.name,
				KeyBindType.this.conflict_context,
				KeyBindType.this.default_key_modifier,
				KeyBindType.this.default_key_code,
				KeyBindType.this.category
			);
			ClientRegistry.registerKeyBinding( this.vanilla_key_bind );
			
			// Clear key code and modifier to avoid key conflict.
			this.vanilla_key_bind.setKeyModifierAndCode(
				KeyModifier.NONE, Keyboard.KEY_NONE );
			
			this.setKeyCodeAndModifier(
				KeyBindType.this.default_key_code,
				KeyBindType.this.default_key_modifier
			);
		}
		
		@Override
		public String identifier() {
			return KeyBindType.this.name;
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
		public void setKeyCodeAndModifier(
			int key_code, KeyModifier key_modifier
		) {
			this.key_code = key_code;
			this.key_modifier = key_modifier;
			
			// Setup active condition.
			final IKeyConflictContext ctx = KeyBindType.this.conflict_context;
			final Supplier< Boolean > vanilla_condition =
				() -> ctx.isActive() && key_modifier.isActive( ctx );
			this.active_condition = (
				KeyBindType.this.depend_on_signal.isEmpty()
				? vanilla_condition
				: () -> {
					// TODO: This is intentional to put inside the lambda, as \
					// otherwise it will fall when this key bind is created \
					// since not all of them are putted into the InputMgr yet.
					final IInput signal = InputSignal.get(
						KeyBindType.this.depend_on_signal );
					return vanilla_condition.get() && signal.asBool();
				}
			);
		}
		
		@Override
		public void update( boolean is_down )
		{
			if ( !is_down && this.is_down )
			{
				this.is_down = false;
				this._onRelease();
			}
			else if ( this.active_condition.get() && is_down && !this.is_down )
			{
				this.is_down = true;
				this._onPress();
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
			this.setKeyCodeAndModifier(
				this.vanilla_key_bind.getKeyCode(),
				this.vanilla_key_bind.getKeyModifier()
			);
			
			this.vanilla_key_bind.setKeyModifierAndCode(
				KeyModifier.NONE, Keyboard.KEY_NONE );
			
			final boolean is_binding_changed = (
				this.key_code != prev_key_code
				|| this.key_modifier != prev_key_modifier
			);
			return(
				is_binding_changed
				? BindingState.CHANGED
				: BindingState.UNCHANGED
			);
		}
		
		protected void _onPress() {
			InputSignal.emitBoolSignal( KeyBindType.this.signal, true );
		}
		
		protected void _onRelease() {
			InputSignal.emitBoolSignal( KeyBindType.this.signal, false );
		}
	}
}
