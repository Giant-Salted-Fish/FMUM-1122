package com.mcwb.common.paintjob;

import com.mcwb.common.load.IContentProvider;
import com.mcwb.common.load.IPostLoadSubscriber;
import com.mcwb.common.meta.IMeta;

/**
 * This provides the ability for third party packs to add paintjobs for {@link IPaintableType}s in
 * other content packs
 * 
 * @author Giant_Salted_Fish
 */
public class ExternalPaintjob extends Paintjob implements IPostLoadSubscriber
{
	protected String injectTarget = "unspecified";
	
	@Override
	public IMeta build( String name, IContentProvider provider)
	{
		super.build( name, provider );
		
		provider.regis( this );
		return this;
	}
	
	@Override
	public void onPostLoad()
	{
		final IPaintableType target = IPaintableType.REGISTRY.get( this.injectTarget );
		if( target != null ) target.injectPaintjob( this );
		else this.warn( "mcwb.expaintjob_target_not_found", this, this.injectTarget );
	}
}
