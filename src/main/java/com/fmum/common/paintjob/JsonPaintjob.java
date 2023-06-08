package com.fmum.common.paintjob;

import com.fmum.common.FMUM;
import com.fmum.common.load.BuildableLoader;
import com.fmum.common.load.IContentProvider;
import com.fmum.common.load.IPostLoadSubscriber;
import com.fmum.common.meta.IMeta;

/**
 * This provides the ability for third party packs to add paintjobs for {@link IPaintableType}s in
 * other content packs.
 * 
 * @author Giant_Salted_Fish
 */
public class JsonPaintjob extends Paintjob implements IPostLoadSubscriber
{
	public static final BuildableLoader< IMeta >
		LOADER = new BuildableLoader<>( "paintjob", JsonPaintjob.class );
	
	protected String injectTarget = "unspecified";
	
	@Override
	public IMeta build( String name, IContentProvider provider)
	{
		super.build( name, provider );
		
		provider.regisPostLoadSubscriber( this );
		return this;
	}
	
	@Override
	public void onPostLoad()
	{
		final IPaintableType target = IPaintableType.REGISTRY.get( this.injectTarget );
		if ( target == null )
		{
			final String translationKey = "fmum.expaintjob_target_not_found";
			FMUM.logWarning( translationKey, this, this.injectTarget );
		}
		else { target.injectPaintjob( this ); }
	}
	
	@Override
	protected IMeta descriptor() { return LOADER; }
}
