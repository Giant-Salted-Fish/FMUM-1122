package gsf.fmum.common.paintjob;

import gsf.fmum.common.FMUM;
import gsf.fmum.common.load.IContentBuildContext;
import gsf.fmum.common.pack.IContentPackFactory.IPostLoadContext;

import java.util.Optional;

public class JsonPaintjob extends Paintjob
{
	protected String inject_target = "unspecified";
	
	@Override
	public void buildServerSide( IContentBuildContext ctx )
	{
		super.buildServerSide( ctx );
		
		ctx.regisPostLoadCallback( this::_injectIntoTarget );
	}
	
	protected void _injectIntoTarget( IPostLoadContext ctx )
	{
		final Optional< IPaintableType > target = IPaintableType
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
