package gsf.fmum.client.input;

import com.google.gson.annotations.SerializedName;
import gsf.fmum.common.load.BuildableType;
import gsf.fmum.common.load.IContentBuildContext;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.Collections;
import java.util.Set;

@SideOnly( Side.CLIENT )
public class KeyBind extends BuildableType implements IKeyBind
{
	protected String category = "fmum.key_category.common";
	
	protected String signal;
	
	@SerializedName( value = "default_key_code", alternate = "key_code" )
	protected int default_key_code = Keyboard.KEY_NONE;
	
	@SerializedName( value = "default_combinations", alternate = "combinations" )
	protected Set< Integer > default_combinations = Collections.emptySet();
	
	protected IKeyConflictContext conflict_context = KeyConflictContext.IN_GAME;
	
	protected transient boolean is_down;
	
	@Override
	public void buildClientSide( IContentBuildContext ctx )
	{
		super.buildClientSide( ctx );
		KeyBindManager.regis( this );
		this.toString();
		
		if ( this.signal == null )
		{
			// Remove prefix that associated to specific pack if present.
			final int idx = this.name.lastIndexOf( '.' );
			this.signal = this.name.substring( idx + 1 );
		}
	}
	
	@Override
	public String identifier() {
		return this.name;
	}
	
	@Override
	public String category() {
		return this.category;
	}
	
	@Override
	public IKeyConflictContext conflictContext() {
		return this.conflict_context;
	}
	
	@Override
	public int defaultKeyCode() {
		return this.default_key_code;
	}
	
	@Override
	public Set< Integer > defaultCombinations() {
		return this.default_combinations;
	}
	
	@Override
	public ActivateResult activate()
	{
		if ( !this.is_down && KeyBind.this.conflict_context.isActive() )
		{
			this.is_down = true;
			this._onPress();
		}
		return ActivateResult.ACTIVATED;
	}
	
	@Override
	public void deactivate()
	{
		if ( this.is_down )
		{
			this.is_down = false;
			this._onRelease();
		}
	}
	
	protected void _onPress() {
		InputManager.emitBoolSignal( KeyBind.this.signal, true );
	}
	
	protected void _onRelease() {
		InputManager.emitBoolSignal( KeyBind.this.signal, false );
	}
	
	@Override
	protected String _typeHint() {
		return "KEY_BIND";
	}
}
