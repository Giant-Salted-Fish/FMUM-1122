package com.fmum.client.input;

import java.util.Collection;

import javax.annotation.Nullable;

import org.lwjgl.input.Keyboard;

import com.fmum.client.FMUMClient;
import com.fmum.client.input.Key.Category;
import com.fmum.client.player.PlayerPatchClient;
import com.fmum.common.load.BuildableMeta;
import com.fmum.common.load.IContentProvider;
import com.fmum.common.meta.IMeta;
import com.fmum.devtool.Dev;
import com.google.gson.annotations.SerializedName;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Default implementation for {@link IKeyBind}.
 * 
 * @author Giant_Salted_Fish
 */
@SideOnly( Side.CLIENT )
public class KeyBind extends BuildableMeta implements IKeyBind
{
	public transient boolean down = false;
	
	protected transient KeyBinding keyBind;
	
	// TODO: maybe add support to parse string key code?
	@SerializedName( value = "keyCode", alternate = { "key", "defaultKey" } )
	protected int keyCode = Keyboard.KEY_NONE;
	
	@SerializedName( value = "category", alternate = "group" )
	protected String category = Category.OTHER;
	
	protected KeyBind() { }
	
	KeyBind( String name, String category, int keyCode ) { this( name, category, keyCode, null ); }
	
	// This public is for develop helper.
	public KeyBind(
		String name,
		String category,
		int keyCode,
		@Nullable Collection< IKeyBind > updateGroup
	) { Dev.dirtyMark();
		this.keyCode = keyCode;
		this.category = category;
		
		this.build( name, FMUMClient.MOD );
		
		// Assign update group.
		if ( updateGroup != null ) { updateGroup.add( this ); }
		else switch ( category )
		{
		case Category.MODIFY:
			InputHandler.GLOBAL_KEYS.add( this );
			break;
			
		case Category.ASSIST:
			InputHandler.CO_KEYS.add( this );
			break;
			
		case Category.GENERAL:
		case Category.GUN:
		case Category.OTHER:
			InputHandler.INCO_KEYS.add( this );
			break;
			
		default: // Should never happen.
			final String prefix = "Unexpected key category <";
			throw new RuntimeException( prefix + this.category + "> from <" + this.name + ">" );
		}
	}
	
	@Override
	public IMeta build( String name, IContentProvider provider )
	{
		super.build( name, provider );
		
		IKeyBind.REGISTRY.regis( this );
		
		final String translationKey = "fmum.key." + this.name;
		this.keyBind = new KeyBinding( translationKey, this.keyCode, this.category );
		ClientRegistry.registerKeyBinding( this.keyBind );
		
		// Clear key code to avoid key conflict.
		this.keyBind.setKeyCode( Keyboard.KEY_NONE );
		return this;
	}
	
	@Override
	public String category() { return this.category; }
	
	@Override
	public void update( boolean down )
	{
		if ( down ^ this.down )
		{
			this.down = down;
			if ( down ) { this.onFire(); }
			else { this.onRelease(); }
		}
	}
	
	@Override
	public void inactiveUpdate( boolean down )
	{
		// Only handle release if it is inactive
		if ( !down && this.down )
		{
			this.down = false;
			this.onRelease();
		}
	}
	
	@Override
	public void restoreMcKeyBind() { this.keyBind.setKeyCode( this.keyCode ); }
	
	@Override
	public boolean clearMcKeyBind()
	{
		final int code = this.keyBind.getKeyCode();
		if ( code == this.keyCode ) { return false; }
		
		this.keyCode = code;
		this.keyBind.setKeyCode( Keyboard.KEY_NONE );
		return true;
	}
	
	@Override
	public int keyCode() { return this.keyCode; }
	
	@Override
	public void setKeyCode( int code ) { this.keyCode = code; }
	
	@Override
	public boolean down() { return this.down; }
	
	// In default just pass the key press notification to player.
	protected void onFire() { PlayerPatchClient.instance.onKeyPress( this ); }
	
	protected void onRelease() { PlayerPatchClient.instance.onKeyRelease( this ); }
	
	@Override
	protected IMeta descriptor() { return () -> "KEYBIND"; }
}
