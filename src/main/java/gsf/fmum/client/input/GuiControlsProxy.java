package gsf.fmum.client.input;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.HashSet;

@SideOnly( Side.CLIENT )
public class GuiControlsProxy extends GuiControls
{
	protected HashSet< Integer > active_key_codes = new HashSet<>();
	protected int last_active_key_code = Keyboard.KEY_NONE;
	
	public GuiControlsProxy( GuiScreen parent_screen, GameSettings settings ) {
		super( parent_screen, settings );
	}
	
	@Override
	public void handleKeyboardInput() throws IOException
	{
		if ( this.buttonId instanceof IFMUMKeyBinding )
		{
			this._handleKeyboardInput( ( IFMUMKeyBinding ) this.buttonId );
			this.mc.dispatchKeypresses();
		}
		else
		{
			// Fallback to original procedure for vanilla key bindings.
			super.handleKeyboardInput();
		}
	}
	
	protected void _handleKeyboardInput( IFMUMKeyBinding key_binding )
	{
		final boolean is_down = Keyboard.getEventKeyState();
		if ( !is_down )
		{
			this._applyKeyBindSetup( key_binding );
			return;
		}
		
		final int key_code = Keyboard.getEventKey();
		final int typed_char = Keyboard.getEventCharacter();
		if ( key_code == Keyboard.KEY_ESCAPE )
		{
			this.last_active_key_code = Keyboard.KEY_NONE;
			this.active_key_codes.clear();
			
			this._applyKeyBindSetup( key_binding );
		}
		else if ( key_code != Keyboard.KEY_NONE ) {
			this._appendActiveCode( key_code );
		}
		else if ( typed_char > ' ' ) {
			this._appendActiveCode( typed_char + 256 );
		}
		
		this.time = Minecraft.getSystemTime();
	}
	
	@Override
	protected void mouseClicked( int mouse_x, int mouse_y, int mouse_button ) throws IOException
	{
		if ( this.buttonId instanceof IFMUMKeyBinding ) {
			this._appendActiveCode( mouse_button - 100 );
		}
		else {
			super.mouseClicked( mouse_x, mouse_y, mouse_button );
		}
	}
	
	@Override
	protected void mouseReleased( int mouseX, int mouseY, int state )
	{
		if (
			this.buttonId instanceof IFMUMKeyBinding
			&& this.last_active_key_code != Keyboard.KEY_NONE
		) {
			this._applyKeyBindSetup( ( IFMUMKeyBinding ) this.buttonId );
		}
		else {
			super.mouseReleased( mouseX, mouseY, state );
		}
	}
	
	protected void _appendActiveCode( int code )
	{
		if ( this.last_active_key_code != Keyboard.KEY_NONE ) {
			this.active_key_codes.add( this.last_active_key_code );
		}
		this.last_active_key_code = code;
	}
	
	protected void _applyKeyBindSetup( IFMUMKeyBinding key_binding )
	{
		key_binding.setKeyCodeAndCombinations(
			this.last_active_key_code, this.active_key_codes );
		KeyBindManager.saveSettings();
		
		this.last_active_key_code = Keyboard.KEY_NONE;
		this.active_key_codes = new HashSet<>();
		this.buttonId = null;
	}
}
