package com.fmum.client.input;

import java.util.Set;

import org.lwjgl.input.Keyboard;

import com.fmum.common.meta.EnumMeta;
import com.fmum.common.meta.TypeBase;
import com.fmum.common.util.LocalAttrParser;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class TypeKeyBind extends TypeBase implements MetaKeyBind
{
	public static final LocalAttrParser< TypeKeyBind > parser = new LocalAttrParser<>( null );
	static
	{
		parser.addKeyword( "Name", ( s, t ) -> t.name = s[ 1 ] );
		parser.addKeyword( "Category", ( s, t ) -> t.category = s[ 1 ] );
		parser.addKeyword(
			"DefaultKey",
			( s, t ) -> {
				if( ( t.keyCode = Keyboard.getKeyIndex( s[ 1 ] ) ) != Keyboard.KEY_NONE )
					return;
				
				try { t.keyCode = Integer.parseInt( s[1] ); }
				catch( NumberFormatException e ) {
					throw new RuntimeException( "Unknown default key <" + s[ 1 ] + ">" );
				}
			}
		);
	}
	
	public int keyCode = Keyboard.KEY_NONE;
	
	public String category = InputHandler.KEY_CATEGORY_OTHER;
	
	public KeyBinding keyBind = null;
	
	/**
	 * Whether this key is down or not
	 */
	public boolean down = false;
	
	/**
	 * Constructor for parse based key bind. If you are creating your own key bind class, it is
	 * recommended to use {@link #TypeKeyBind(String, int, String)} rather than this one.
	 * 
	 * @note This constructor will not initialize {@link #keyBind}
	 * @param name Name of the key
	 */
	protected TypeKeyBind( String name ) { super( name ); }
	
	/**
	 * For {@link InputHandler} use only to create default keys. If you are a content pack producer
	 * please use {@link #TypeKeyBind(String)} instead. This constructor will call
	 * {@link #onPostInit()} as it will not go through the post initialization life-cycle.
	 */
	TypeKeyBind( String name, int keyCode, String category )
	{
		super( name );
		
		this.keyCode = keyCode;
		this.category = category;
		this.onPostInit();
	}
	
	@Override
	public void regisPostInitHandler( Set< Runnable > tasks )
	{
		super.regisPostInitHandler( tasks );
		
		tasks.add(
			() -> {
				ClientRegistry.registerKeyBinding(
					this.keyBind = new KeyBinding(
						"fmum.key." + this.name, // Add a "fmum.key." prefix to avoid name conflict
						Keyboard.KEY_NONE, // Only set the real key code when controls GUI is launched
						this.category
					)
				);
			}
		);
	}
	
	@Override
	public void showMCKeyBind() { this.keyBind.setKeyCode( this.keyCode ); }
	
	@Override
	public boolean clearMCKeyBind()
	{
		if( this.keyBind.getKeyCode() == this.keyCode )
			return false;
		
		this.keyCode = this.keyBind.getKeyCode();
		this.keyBind.setKeyCode( Keyboard.KEY_NONE );
		return true;
	}
	
	@Override
	public int keyCode() { return this.keyCode; }
	
	@Override
	public void $keyCode( int code ) { this.keyCode = code; }
	
	@Override
	public boolean down() { return this.down; }
	
	@Override
	public void $down( boolean down ) { this.down = down; }
	
	@Override
	public String category() { return this.category; }
	
	@Override
	public EnumMeta enumMeta() { return EnumMeta.KEY_BIND; }
}
