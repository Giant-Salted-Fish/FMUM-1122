package com.fmum.paintjob;

import com.fmum.FMUM;
import com.fmum.load.IContentBuildContext;
import com.fmum.load.IContentLoader;
import com.fmum.load.IPostLoadContext;
import com.fmum.load.JsonData;
import com.google.gson.annotations.Expose;

import java.util.Optional;

public class JsonPaintjob extends Paintjob
{
	public static final IContentLoader< JsonPaintjob >
		LOADER = IContentLoader.of( JsonPaintjob::new );
	
	
	@Expose
	protected String inject_target;
	
	@Override
	public void build( JsonData data, String fallback_name, IContentBuildContext ctx )
	{
		super.build( data, fallback_name, ctx );
		
		ctx.regisPostLoadCallback( this::_injectPaintjob );
	}
	
	@Override
	public void reload( JsonData data, IContentBuildContext ctx )
	{
		super.reload( data, ctx );
		
		this.inject_target = data.getString( "inject_target" ).orElse( "unspecified" );
	}
	
	protected void _injectPaintjob( IPostLoadContext ctx )
	{
		final Optional< IPaintableType > target = IPaintableType.REGISTRY.lookup( this.inject_target );
		if ( target.isPresent() ) {
			target.get().injectPaintjob( this );
		}
		else {
			FMUM.LOGGER.error( "Can not find paintjob inject target <{}> required by <{}>.", this.inject_target, this );
		}
	}
}
