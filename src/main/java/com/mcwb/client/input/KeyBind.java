package com.mcwb.client.input;

import java.util.Collection;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.google.gson.annotations.SerializedName;
import com.mcwb.client.input.Key.KeyCategory;
import com.mcwb.client.player.PlayerPatchClient;
import com.mcwb.common.MCWB;
import com.mcwb.common.load.BuildableLoader;
import com.mcwb.common.load.BuildableMeta;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.pack.IContentProvider;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class KeyBind extends BuildableMeta implements IKeyBind
{
	public static final BuildableLoader< IMeta >
		LOADER = new BuildableLoader<>( "key_bind", ExternalKeyBind.class );
	
	protected static final Function< IKeyBind, Boolean >
		KEY_UPDATER = key -> Keyboard.isKeyDown( key.keyCode() );
	
	protected static final Function< IKeyBind, Boolean >
		MOUSE_UPDATER = key -> Mouse.isButtonDown( key.keyCode() + 100 );
	
	protected static final Function< IKeyBind, Boolean >
		ABSENT_UPDATER = key -> false;
	
	/**
	 * Whether this key is down or not
	 */
	public transient boolean down = false;
	
	@SerializedName( value = "keyCode", alternate = { "key", "defaultKey" } )
	protected int keyCode = Keyboard.KEY_NONE;
	
	protected transient Function< IKeyBind, Boolean > updater = ABSENT_UPDATER;
	
	@SerializedName( value = "category", alternate = "group" )
	protected String category = KeyCategory.OTHER;
	
	protected transient KeyBinding keyBind;
	
	protected KeyBind() { }
	
	public KeyBind( String name, String category, int keyCode ) {
		this( name, category, keyCode, null );
	}
	
	public KeyBind(
		String name,
		String category,
		int keyCode,
		@Nullable Collection< IKeyBind > updateGroup
	) {
		this.name = name;
		this.provider = MCWB.MOD;
		
		this.$keyCode( keyCode );
		this.category = category;
		this.build( MCWB.MODID, MCWB.MOD );
		
		/// Assign update group
		// If group specified, add it to the group
		if( updateGroup != null )
			updateGroup.add( this );
		
		// Otherwise, assign group based on its category
		else switch( category )
		{
		case KeyCategory.MODIFY:
			InputHandler.GLOBAL_KEYS.add( this );
			break;
			
		case KeyCategory.ASSIST:
			InputHandler.CO_KEYS.add( this );
			break;
			
		case KeyCategory.GENERAL:
		case KeyCategory.GUN:
		case KeyCategory.OTHER:
			InputHandler.INCO_KEYS.add( this );
			break;
			
		default: // This should never happen
			throw new RuntimeException(
				"Unexpected key category <" + this.category + "> from <" + this.name
			);
		}
	}
	
	@Override
	public IMeta build( String name, IContentProvider provider )
	{
		super.build( name, provider );
		
		IKeyBind.REGISTRY.regis( this );
		
		this.keyBind = new KeyBinding(
			"mcwb.key." + this.name, // Add a prefix to avoid conflict
			this.keyCode,
			this.category
		);
		
		// Clear key code to avoid key conflict?
		this.keyBind.setKeyCode( Keyboard.KEY_NONE );
		
		ClientRegistry.registerKeyBinding( this.keyBind );
		return this;
	}
	
	@Override
	public void update()
	{
		if( this.down ^ this.updater.apply( this ) )
		{
			if( this.down ) this.release();
			else this.fire();
			this.down = !this.down;
		}
	}
	
	@Override
	public void reset() { this.down = false; }
	
	@Override
	public void restoreMcKeyBind() { this.keyBind.setKeyCode( this.keyCode ); }
	
	@Override
	public boolean clearMcKeyBind()
	{
		final int code = this.keyBind.getKeyCode();
		if( code == this.keyCode ) return false;
		
		this.$keyCode( code );
		this.keyBind.setKeyCode( Keyboard.KEY_NONE );
		return true;
	}
	
	@Override
	public int keyCode() { return this.keyCode; }
	
	@Override
	public void $keyCode( int code )
	{
		this.keyCode = code;
		this.updater = code > 0 ? KEY_UPDATER : code < 0 ? MOUSE_UPDATER : ABSENT_UPDATER;
	}
	
	@Override
	public boolean down() { return this.down; }
	
	@Override
	public String category() { return this.category; }
	
	/**
	 * In default just pass the key press notification to player
	 */
	protected void fire() { PlayerPatchClient.instance.onKeyPress( this ); }
	
	protected void release() { PlayerPatchClient.instance.onKeyRelease( this ); }
	
	@Override
	protected IMeta loader() { return LOADER; }
}
