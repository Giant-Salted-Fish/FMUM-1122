package com.fmum.common.paintjob;

import com.fmum.common.FMUM;
import com.fmum.common.load.ContentBuildContext;
import com.fmum.common.pack.ContentPackFactory.PostLoadContext;

import java.util.Optional;

public class JsonPaintjob extends CPaintjob
{
	protected String inject_target = "unspecified";
	
	@Override
	public void buildServerSide( ContentBuildContext ctx )
	{
		super.buildServerSide( ctx );
		
		ctx.regisPostLoadCallback( this::_injectIntoTarget );
	}
	
	protected void _injectIntoTarget( PostLoadContext ctx )
	{
		final Optional< PaintableType > target = PaintableType
			.REGISTRY.lookup( this.inject_target );
		if ( target.isPresent() ) {
			target.get().injectPaintjob( this ); }
		else
		{
			FMUM.MOD.logError(
				"fmum.paintjob_inject_target_not_found",
				this.toString(), this.inject_target );
		}
	}
}
