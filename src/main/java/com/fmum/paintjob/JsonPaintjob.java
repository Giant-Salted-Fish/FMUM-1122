package com.fmum.paintjob;

import com.fmum.FMUM;
import com.fmum.load.IContentBuildContext;
import com.fmum.load.IPostLoadContext;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import java.util.Optional;

public class JsonPaintjob extends Paintjob
{
	@Expose
	protected String inject_target = "unspecified";
	
	@Override
	public void build( JsonObject data, String fallback_name, IContentBuildContext ctx )
	{
		super.build( data, fallback_name, ctx );
		
		ctx.regisPostLoadCallback( this::_injectPaintjob );
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
