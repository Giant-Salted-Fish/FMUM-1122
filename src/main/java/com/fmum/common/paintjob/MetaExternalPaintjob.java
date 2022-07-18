package com.fmum.common.paintjob;

import java.util.Map;

import com.fmum.common.meta.EnumMeta;
import com.fmum.common.meta.MetaBase;

/**
 * Third party paintjobs that will be inject into {@link MetaPaintable} in post load phase
 * 
 * @see MetaPaintable
 * @author Giant_Salted_Fish
 */
public interface MetaExternalPaintjob extends MetaBase
{
	@Override
	public default void regisPostInitHandler( Map< String, Runnable > tasks ) {
		MetaBase.super.regisPostInitHandler( tasks );
	}
	
	@Override
	public default void regisPostLoadHandler( Map< String, Runnable > tasks )
	{
		MetaBase.super.regisPostLoadHandler( tasks );
		
		// Inject this paintjob
		tasks.put( "INJECT_PAINTJOB", () -> this.inject() );
	}
	
	public default void inject()
	{
		MetaPaintable target = MetaPaintable.get( this.injectTarget() );
		if( target != null )
			target.injectPaintjob( this );
		else this.log().warn(
			this.format(
				"fmum.cannotfindpaintjobinjecttarget",
				this.toString(),
				this.injectTarget()
			)
		);
	}
	
	/**
	 * For default case, assuming that this external paintjob has only one inject target which can
	 * be find by its name. In this circumstance this can be complete automatically. Otherwise you
	 * will need to override {@link #inject()} and handle injection yourself
	 * 
	 * @return Name of the {@link MetaPaintable} to inject
	 */
	public String injectTarget();
	
	@Override
	public default EnumMeta enumMeta() { return EnumMeta.EX_PAINTJOB; }
}
