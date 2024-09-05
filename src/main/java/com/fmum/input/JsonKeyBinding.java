package com.fmum.input;

import com.fmum.load.BuildableType;
import com.fmum.load.IContentBuildContext;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.kbp.client.KBPMod;
import com.kbp.client.api.IPatchedKeyBinding;
import com.kbp.client.api.KeyBindingBuilder;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly( Side.CLIENT )
public class JsonKeyBinding extends BuildableType
{
	@Expose
	protected String category = "key_category.fmum_common";
	
	@Expose
	protected String signal;
	
	@Expose
	protected int key_code = Keyboard.KEY_NONE;
	
	@Expose
	protected Integer[] combinations = { };
	
	@Expose
	protected IKeyConflictContext conflict_context = KeyConflictContext.IN_GAME;
	
	@Expose
	protected boolean is_toggle = false;
	
	@Override
	public void build( JsonObject data, String fallback_name, IContentBuildContext ctx )
	{
		super.build( data, fallback_name, ctx );
		
		if ( this.signal == null )
		{
			// Remove prefix that associated to specific pack if present.
			final int idx = this.name.lastIndexOf( '.' );
			this.signal = this.name.substring( idx + 1 );
		}
		
		this._buildKeyBinding();
	}
	
	protected void _buildKeyBinding()
	{
		final KeyBindingBuilder builder = KBPMod.newBuilder( this.name );
		builder.withCategory( this.category );
		builder.withKey( this.key_code );
		builder.withCmbKeys( this.combinations );
		builder.withConflictContext( this.conflict_context );
		
		final IPatchedKeyBinding key_binding = builder.buildAndRegis();
		final String signal = this.signal;
		if ( this.is_toggle )
		{
			key_binding.addPressCallback( new Runnable() {
				private boolean state_flag;
				
				@Override
				public void run()
				{
					final boolean new_state = !this.state_flag;
					InputManager.emitBoolSignal( signal, new_state );
					this.state_flag = new_state;
				}
			} );
		}
		else
		{
			key_binding.addPressCallback( () -> InputManager.emitBoolSignal( signal, true ) );
			key_binding.addReleaseCallback( () -> InputManager.emitBoolSignal( signal, false ) );
		}
	}
}
