package com.fmum.input;

import com.fmum.load.BuildableType;
import com.fmum.load.IContentBuildContext;
import com.fmum.load.IContentLoader;
import com.fmum.load.JsonData;
import com.google.gson.JsonObject;
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
	public static final IContentLoader< JsonKeyBinding >
		LOADER = IContentLoader.of( JsonKeyBinding::new );
	
	
	// TODO: conflict context for gun and mag.
	protected String category;
	
	protected String signal;
	
	protected int key_code;
	
	protected Integer[] combinations;
	
	protected IKeyConflictContext conflict_context;
	
	protected boolean is_toggle;
	
	@Override
	public void build( JsonObject data, String fallback_name, IContentBuildContext ctx )
	{
		super.build( data, fallback_name, ctx );
		
		this._buildKeyBinding();
	}
	
	@Override
	public void reload( JsonObject json, IContentBuildContext ctx )
	{
		super.reload( json, ctx );
		
		final JsonData data = new JsonData( json, ctx.getGson() );
		this.category = data.getString( "category" ).orElse( "key_category.fmum_common" );
		this.signal = data.getString( "signal" ).orElseGet( () -> {
			// Remove prefix that associated to specific pack if present.
			final int idx = this.name.lastIndexOf( '.' );
			return this.name.substring( idx + 1 );
		} );
		this.key_code = data.getInt( "key_code" ).orElse( Keyboard.KEY_NONE );
		this.combinations = data.get( "combinations", Integer[].class ).orElse( new Integer[0] );
		this.conflict_context = data.get( "conflict_context", IKeyConflictContext.class ).orElse( KeyConflictContext.IN_GAME );
		this.is_toggle = data.getBool( "is_toggle" ).orElse( false );
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
