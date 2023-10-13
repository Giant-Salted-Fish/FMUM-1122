package com.fmum.client.input;

import com.fmum.client.FMUMClient;
import com.fmum.client.input.IKeyBind.ActivateResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.MouseInputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@SideOnly( Side.CLIENT )
@EventBusSubscriber( modid = FMUMClient.MODID, value = Side.CLIENT )
public final class KeyBindManager
{
	private static final HashMap< String, _FMUMKeyBinding >
		REGISTRY = new HashMap<>();
	
	private static final HashMap< Integer, _CombinationKey >
		COMBINATION_TABLE = new HashMap<>();
	
	private static final HashMap< Integer, List< _FMUMKeyBinding > >
		UPDATE_TABLE = new HashMap<>();
	
	private static final Gson GSON;
	static
	{
		final GsonBuilder builder =  new GsonBuilder();
		builder.setLenient();
		builder.setPrettyPrinting();
		GSON = builder.create();
	}
	
	private static File key_bind_setting_file;
	
	private KeyBindManager() { }
	
	@SubscribeEvent
	static void _onKeyboardInput( KeyInputEvent evt )
	{
		final int key_code = Keyboard.getEventKey();
		final int typed_char = Keyboard.getEventCharacter();
		final int key = key_code == Keyboard.KEY_NONE ? typed_char + 256 : key_code;
		final boolean is_down = Keyboard.getEventKeyState();
		__dispatchInput( key, is_down );
	}
	
	@SubscribeEvent
	static void _onMouseInput( MouseInputEvent evt )
	{
		final int button = Mouse.getEventButton() - 100;
		final boolean is_down = Mouse.getEventButtonState();
		__dispatchInput( button, is_down );
	}
	
	public static void regis( IKeyBind key_bind )
	{
		final _FMUMKeyBinding key_binding = new _FMUMKeyBinding( key_bind );
		REGISTRY.put( key_bind.identifier(), key_binding );
		ClientRegistry.registerKeyBinding( key_binding );
	}
	
	public static void loadSettingsFrom( File settings_file )
	{
		key_bind_setting_file = settings_file;
		if ( settings_file.exists() )
		{
			__loadSettings();
			return;
		}
		
		try {
			settings_file.createNewFile();
		}
		catch ( IOException e )
		{
			final String err_msg = "fmum.error_creating_key_binds_file";
			FMUMClient.MOD.logException( e, err_msg );
			return;
		}
		
		saveSettings();
	}
	
	public static void saveSettings()
	{
		try ( FileWriter out = new FileWriter( key_bind_setting_file ) )
		{
			final HashMap< String, Object > data = new HashMap<>();
			REGISTRY.values().forEach( kb -> {
				final ArrayList< Integer > codes = new ArrayList<>();
				codes.add( kb.key_code );
				codes.addAll( kb.combinations );
				data.put( kb.key_bind.identifier(), codes );
			} );
			out.write( GSON.toJson( data ) );
		}
		catch ( IOException e ) {
			FMUMClient.MOD.logException( e, "fmum.error_saving_key_binds" );
		}
	}
	
	private static void __loadSettings()
	{
		try ( FileReader in = new FileReader( key_bind_setting_file ) )
		{
			final JsonObject data = GSON.fromJson( in, JsonObject.class );
			data.entrySet().forEach( e -> {
				final String id = e.getKey();
				final _FMUMKeyBinding kb = REGISTRY.get( id );
				if ( kb != null )
				{
					try
					{
						final JsonArray arr = e.getValue().getAsJsonArray();
						final Iterator< JsonElement > itr = arr.iterator();
						final int key_code = itr.next().getAsInt();
						final HashSet< Integer > combinations = new HashSet<>();
						while ( itr.hasNext() ) {
							combinations.add( itr.next().getAsInt() );
						}
						
						kb.setKeyCodeAndCombinations( key_code, combinations );
					}
					catch ( Exception e_ )
					{
						final String err_msg = "fmum.error_reading_key_bind_setting";
						FMUMClient.MOD.logException( e_, err_msg, id );
					}
				}
				else
				{
					final String err_msg = "fmum.unrecognized_key_bind";
					FMUMClient.MOD.logError( err_msg, id );
				}
			} );
		}
		catch ( IOException e )
		{
			final String err_msg = "fmum.error_reading_key_binds";
			FMUMClient.MOD.logException( e, err_msg );
		}
	}
	
	private static Supplier< Boolean > __subscribeCombination( Integer key_code )
	{
		final _CombinationKey combination = COMBINATION_TABLE.compute(
			key_code,
			( code, ck ) -> {
				if ( ck == null )
				{
					ck = new _CombinationKey();
				}
				ck.ref_count += 1;
				return ck;
			}
		);
		return () -> combination.is_down;
	}
	
	private static void __releaseCombination( Integer key_code )
	{
		COMBINATION_TABLE.compute( key_code, ( code, ck ) -> {
			assert ck != null;
			ck.ref_count -= 1;
			return ck.ref_count <= 0 ? null : ck;
		} );
	}
	
	private static void __removeFromTable( _FMUMKeyBinding key_binding )
	{
		UPDATE_TABLE.computeIfPresent( key_binding.key_code, ( c, lst ) -> {
			// TODO: Maybe with binary search?
			lst.remove( key_binding );
			return lst.isEmpty() ? null : lst;
		} );
	}
	
	private static void __addToTable( _FMUMKeyBinding key_binding )
	{
		UPDATE_TABLE.compute( key_binding.key_code, ( c, lst ) -> {
			if ( lst == null ) {
				lst = new ArrayList<>();
			}
			
			final int priority = key_binding._priority();
			
			// TODO: Maybe with binary search?
			int idx = 0;
			final int size = lst.size();
			while ( idx < size )
			{
				final int cur_pri = lst.get( idx )._priority();
				if ( priority > cur_pri ) {
					break;
				}
				
				idx += 1;
			}
			
			lst.add( idx, key_binding );
			return lst;
		} );
	}
	
	private static void __dispatchInput( Integer code, boolean is_down )
	{
		final _CombinationKey ck = COMBINATION_TABLE.get( code );
		if ( ck != null ) {
			ck.is_down = is_down;
		}
		
		final List< _FMUMKeyBinding > lst = UPDATE_TABLE.getOrDefault( code, Collections.emptyList() );
		if ( is_down )
		{
			final Iterator< _FMUMKeyBinding > itr = lst.iterator();
			while ( itr.hasNext() )
			{
				final _FMUMKeyBinding kb = itr.next();
				if ( !kb._isCombinationActive() ) {
					continue;
				}
				
				final ActivateResult result = kb.key_bind.activate();
				if ( result == ActivateResult.PASS ) {
					continue;
				}
				
				final int priority = kb._priority();
				while ( itr.hasNext() )
				{
					final _FMUMKeyBinding kb_ = itr.next();
					if ( kb_._priority() != priority ) {
						break;
					}
					
					kb_.key_bind.activate();
				}
				break;
			}
		}
		else {
			lst.forEach( kb -> kb.key_bind.deactivate() );
		}
	}
	
	
	protected static class _CombinationKey
	{
		protected int ref_count = 0;
		protected boolean is_down = false;
	}
	
	protected static class _FMUMKeyBinding
		extends KeyBinding implements IFMUMKeyBinding
	{
		protected final IKeyBind key_bind;
		
		protected int key_code = Keyboard.KEY_NONE;
		
		protected Set< Integer > combinations = Collections.emptySet();
		
		protected final LinkedList< Supplier< Boolean > >
			active_conditions = new LinkedList<>();
		
		protected _FMUMKeyBinding( IKeyBind key_bind )
		{
			super(
				key_bind.identifier(),
				key_bind.conflictContext(),
				KeyModifier.NONE,
				Keyboard.KEY_NONE,
				key_bind.category()
			);
			
			this.key_bind = key_bind;
			
			final int key_code = key_bind.defaultKeyCode();
			final Set< Integer > combinations = key_bind.defaultCombinations();
			this.setKeyCodeAndCombinations( key_code, combinations );
		}
		
		@Override
		public void setKeyCodeAndCombinations(
			int key_code,
			Set< Integer > combinations
		) {
			if ( this.key_code != Keyboard.KEY_NONE )
			{
				__removeFromTable( this );
				this.combinations.forEach( KeyBindManager::__releaseCombination );
			}
			
			if ( key_code != Keyboard.KEY_NONE )
			{
				this.key_code = key_code;
				this.combinations = combinations;
				this._regisToUpdateTable();
			}
		}
		
		@Override
		public void setToDefault()
		{
			final int code = this.key_bind.defaultKeyCode();
			final Set< Integer > cmb = this.key_bind.defaultCombinations();
			this.setKeyCodeAndCombinations( code, cmb );
		}
		
		@Override
		public boolean isSetToDefaultValue()
		{
			final int code = this.key_bind.defaultKeyCode();
			final Set< Integer > cmb = this.key_bind.defaultCombinations();
			return this.key_code == code && this.combinations.equals( cmb );
		}
		
		@Override
		public String getDisplayName()
		{
			final String key_code = GameSettings.getKeyDisplayString( this.key_code );
			return this.combinations.stream()
				.map( GameSettings::getKeyDisplayString )
				.reduce( ( s0, s1 ) -> s0 + " + " + s1 )
				.map( str -> str + " + " + key_code )
				.orElse( key_code );
		}
		
		protected int _priority() {
			return this.combinations.size();
		}
		
		protected boolean _isCombinationActive()
		{
			for ( Supplier< Boolean > condition : this.active_conditions )
			{
				if ( !condition.get() ) {
					return false;
				}
			}
			return true;
		}
		
		protected void _regisToUpdateTable()
		{
			__addToTable( this );
			
			this.active_conditions.clear();
			this.combinations.forEach( code ->
				this.active_conditions.add( __subscribeCombination( code ) )
			);
		}
	}
}
