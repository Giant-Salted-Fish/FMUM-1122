package com.fmum.paintjob;

import com.fmum.FMUM;
import com.fmum.load.IContentBuildContext;
import com.fmum.load.IContentLoader;
import com.fmum.load.IPostLoadContext;
import com.fmum.load.JsonData;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import java.util.Optional;

public class JsonPaintjob extends Paintjob
{
	public static final IContentLoader< JsonPaintjob >
		LOADER = IContentLoader.of( JsonPaintjob::new );
	
	
	@Expose
	protected String inject_target;
	
	@Override
	public void build( JsonObject data, String fallback_name, IContentBuildContext ctx )
	{
		super.build( data, fallback_name, ctx );
		
		ctx.regisPostLoadCallback( this::_injectPaintjob );
	}
	
	@Override
	public void reload( JsonObject json, IContentBuildContext ctx )
	{
		super.reload( json, ctx );
		
		final JsonData data = new JsonData( json, ctx.getGson() );
		this.inject_target = data.getString( "inject_target" ).orElse( "unspecified" );
	}
	
	protected void _injectPaintjob( IPostLoadContext ctx )
	{
		final Optional< IPaintableType > target = IPaintableType.REGISTRY.lookup( this.inject_target );
		if ( target.isPresent() )
		{
			target.get().injectPaintjob( this );
			return;
		}
		
		final String translate_key = "fmum.paintjob_inject_target_not_found";
		FMUM.LOGGER.error( translate_key, this, this.inject_target );
	}
}
