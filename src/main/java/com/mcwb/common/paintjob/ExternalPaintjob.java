package com.mcwb.common.paintjob;

import com.mcwb.common.load.IRequirePostLoad;
import com.mcwb.common.meta.Meta;
import com.mcwb.common.modify.IModifiableMeta;
import com.mcwb.common.pack.IContentProvider;

/**
 * This provides the ability for third party packs to add paintjobs for {@link IModifiableMeta}s in
 * other packs
 * 
 * @author Giant_Salted_Fish
 */
public class ExternalPaintjob extends Paintjob implements IRequirePostLoad
{
	protected String injectTarget = "unspecified";
	
	@Override
	public Meta build( String name, IContentProvider provider )
	{
		super.build( name, provider );
		
		provider.regisPostLoad( this );
		return this;
	}
	
	@Override
	public void onPostLoad()
	{
		final IModifiableMeta target = IModifiableMeta.REGISTRY.get( this.injectTarget );
		if( target != null ) target.injectPaintjob( this ); // TODO: maybe a null object to send warning
		else this.warn( "mcwb.expaintjob_target_not_found", this, this.injectTarget );
	}
}
