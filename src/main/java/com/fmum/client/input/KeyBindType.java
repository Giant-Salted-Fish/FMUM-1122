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
	protected String category = "fmum.key_category.common";
	
	protected String signal;
	
	@SerializedName( value = "depend_on_signal", alternate = "depend_on" )
	protected String depend_on = "";
	
	@SerializedName( value = "default_key_code", alternate = "key_code" )
	protected int default_key_code = Keyboard.KEY_NONE;
	
	@SerializedName( value = "default_key_modifier", alternate = "key_modifier" )
	protected KeyModifier default_key_modifier = KeyModifier.NONE;
	
	protected IKeyConflictContext conflict_context = KeyConflictContext.IN_GAME;
	
	@Override
	public void buildClientSide( IContentBuildContext ctx )
	{
		super.buildClientSide( ctx );
		
		this.signal = Optional.ofNullable( this.signal ).orElseGet( () -> {
			// Remove prefix that associated to specific pack if present.
			final int idx = this.name.indexOf( '.' );
			return this.name.substring( Math.max( 0, idx ) );
		} );
		
		final KeyBind kb = this._createKeyBind();
		ctx.regisPostLoadCallback( c -> kb._setupDependency() );
	}
	
	protected KeyBind _createKeyBind() {
		return new KeyBind();
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
		
		protected Supplier< Boolean > depend_on_is_down = () -> true;
		
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
			) {
				@Override
				public String getDisplayName() {
					return KeyBind.this.activeConditionRepr();
				}
			};
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
		public void setKeyCodeAndModifier(
			int key_code, KeyModifier key_modifier
		) {
			this.key_code = key_code;
			this.key_modifier = key_modifier;
		}
		
		@Override
		public String activeConditionRepr()
		{
			final String repr = this.key_modifier
				.getLocalizedComboName( this.key_code );
			final Optional< IKeyBind > depend_on = IKeyBind
				.REGISTRY.lookup( KeyBindType.this.depend_on );
			return depend_on.map(
				kb -> kb.activeConditionRepr() + " + " + repr ).orElse( repr );
		}
		
		@Override
		public void update( boolean is_down )
		{
			if ( !is_down && this.is_down )
			{
				this.is_down = false;
				this._onRelease();
			}
			else if (
				is_down && !this.is_down
				&& KeyBindType.this.conflict_context.isActive()
				&& this.key_modifier.isActive(
					KeyBindType.this.conflict_context )
				&& this.depend_on_is_down.get()
			) {
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
		
		protected void _setupDependency()
		{
			if ( KeyBindType.this.depend_on.isEmpty() ) {
				return;
			}
			
			final Optional< IKeyBind > depend_on = IKeyBind
				.REGISTRY.lookup( KeyBindType.this.depend_on );
			this.depend_on_is_down = depend_on.map(
				kb -> ( Supplier< Boolean > ) kb::isDown
			).orElseGet( () -> {
				FMUMClient.MOD.logError(
					"fmum.depend_on_key_not_found",
					KeyBindType.this.name,
					KeyBindType.this.depend_on
				);
				return () -> true;
			} );
		}
		
		protected void _onPress() {
			InputSignal.emitBoolSignal( KeyBindType.this.signal, true );
		}
		
		protected void _onRelease() {
			InputSignal.emitBoolSignal( KeyBindType.this.signal, false );
		}
	}
}
