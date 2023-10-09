package com.fmum.client.input;

import com.fmum.client.FMUMClient;
import com.fmum.common.load.BuildableType;
import com.fmum.common.load.IContentBuildContext;
import com.fmum.common.pack.IContentPackFactory.IPostLoadContext;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@SideOnly( Side.CLIENT )
public class KeyBindType extends BuildableType
{
	protected String category = "fmum.key_category.common";
	
	protected String signal;
	
	@SerializedName( value = "default_key_code", alternate = "key_code" )
	protected int default_key_code = Keyboard.KEY_NONE;
	
	@SerializedName( value = "default_key_modifier", alternate = "key_modifier" )
	protected KeyModifier default_key_modifier = KeyModifier.NONE;
	
	protected Set< String > combinations = Collections.emptySet();
	
	protected IKeyConflictContext conflict_context = KeyConflictContext.IN_GAME;
	
	@Override
	public void buildClientSide( IContentBuildContext ctx )
	{
		super.buildClientSide( ctx );
		
		if ( this.signal == null )
		{
			// Remove prefix that associated to specific pack if present.
			final int idx = this.name.lastIndexOf( '.' );
			this.signal = this.name.substring( idx + 1 );
		}
		
		this._createKeyBind( ctx );
	}
	
	protected void _createKeyBind( IContentBuildContext ctx )
	{
		final KeyBind kb = new KeyBind();
		ctx.regisPostLoadCallback( kb::_setupCombinations );
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
		
		protected final HashSet< IKeyBind > combinations = new HashSet<>();
		
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
					return KeyBind.this.boundenKeyRepr();
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
		public String boundenKeyRepr()
		{
			String repr = this.key_modifier.getLocalizedComboName( this.key_code );
			for ( IKeyBind cb : this.combinations ) {
				repr = String.format( "%s + %s", cb.boundenKeyRepr(), repr );
			}
			return repr;
		}
		
		@Override
		public UpdateResult update( boolean is_down )
		{
			if ( !is_down && this.is_down )
			{
				this.is_down = false;
				this._onRelease();
				return UpdateResult.PASS;
			}
			
			final IKeyConflictContext conflict_ctx =
				KeyBindType.this.conflict_context;
			if (
				is_down && !this.is_down
					&& conflict_ctx.isActive()
					&& this.key_modifier.isActive( conflict_ctx )
					&& this._isCombinationsActive()
			) {
				this.is_down = true;
				this._onPress();
				return UpdateResult.CONSUMED;
			}
			
			return UpdateResult.PASS;
		}
		
		protected final boolean _isCombinationsActive()
		{
			for ( IKeyBind cb : this.combinations )
			{
				if ( !cb.isDown() ) {
					return false;
				}
			}
			return true;
		}
		
		protected void _onPress() {
			InputManager.emitBoolSignal( KeyBindType.this.signal, true );
		}
		
		protected void _onRelease() {
			InputManager.emitBoolSignal( KeyBindType.this.signal, false );
		}
		
		@Override
		public int priority()
		{
			int priority = 0;
			priority += this.key_code != Keyboard.KEY_NONE ? 1 : 0;
			priority += this.key_modifier != KeyModifier.NONE ? 1 : 0;
			priority += this.combinations.size();
			return priority;
		}
		
		@Override
		public void restoreVanillaKeyBind()
		{
			this.vanilla_key_bind.setKeyModifierAndCode(
				this.key_modifier, this.key_code );
		}
		
		@Override
		public ClearState clearVanillaKeyBind()
		{
			final int prev_key_code = this.key_code;
			final KeyModifier prev_key_modifier = this.key_modifier;
			this.setKeyCodeAndModifier(
				this.vanilla_key_bind.getKeyCode(),
				this.vanilla_key_bind.getKeyModifier()
			);
			
			this.vanilla_key_bind.setKeyModifierAndCode(
				KeyModifier.NONE, Keyboard.KEY_NONE );
			
			final boolean flag = (
				this.key_code != prev_key_code
					|| this.key_modifier != prev_key_modifier
			);
			return flag ? ClearState.CHANGED : ClearState.UNCHANGED;
		}
		
		@Override
		public Object serialize() {
			return this.key_code + "+" + this.key_modifier;
		}
		
		@Override
		public void deserialize( JsonElement data )
		{
			final String[] setting = data.getAsString().split( "\\+" );
			final int key_code = Integer.parseInt( setting[ 0 ] );
			final KeyModifier modifier = KeyModifier.valueFromString( setting[ 1 ] );
			this.setKeyCodeAndModifier( key_code, modifier );
		}
		
		protected void _setupCombinations( IPostLoadContext ctx )
		{
			this.combinations.clear();
			KeyBindType.this.combinations.forEach( id -> {
				final Optional< IKeyBind > kb = IKeyBind.REGISTRY.lookup( id );
				if ( kb.isPresent() ) {
					this.combinations.add( kb.get() );
				}
				else
				{
					final String err_msg = "fmum.can_not_find_combination_key_bind";
					FMUMClient.MOD.logError( err_msg, this, id );
				}
			} );
		}
	}
}
