package com.mcwb.common.paintjob;

import com.mcwb.common.load.BuildableLoader;
import com.mcwb.common.load.IContentProvider;
import com.mcwb.common.load.IPostLoadSubscriber;
import com.mcwb.common.meta.IMeta;

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
			final String translationKey = "mcwb.expaintjob_target_not_found";
			this.logWarning( translationKey, this, this.injectTarget );
		}
		else { target.injectPaintjob( this ); }
	}
	
	@Override
	protected IMeta descriptor() { return LOADER; }
}
