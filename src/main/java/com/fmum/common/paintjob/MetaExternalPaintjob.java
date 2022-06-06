package com.fmum.common.paintjob;

import java.util.Set;
import java.util.TreeSet;

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
	public static final TreeSet< MetaExternalPaintjob > waitInjection = new TreeSet<>();
	
	@Override
	public default void regisPostInitHandler( Set< Runnable > tasks )
	{
		MetaBase.super.regisPostInitHandler( tasks );
		
		tasks.add( () -> waitInjection.add( this ) );
	}
	
	@Override
	public default void regisPostLoadHandler( Set< Runnable > tasks )
	{
		MetaBase.super.regisPostLoadHandler( tasks );
		
		// Inject all paint jobs if have not yet
		tasks.add( () -> {
			if( waitInjection.isEmpty() ) return;
			
			for( MetaExternalPaintjob paintjob : waitInjection )
				paintjob.inject();
			waitInjection.clear();
		} );
	}
	
	public default void inject()
	{
		MetaPaintable target = MetaPaintable.get( this.injectTarget() );
		if( target != null )
			target.injectPaintjob( this );
		else this.log().warn(
			this.format(
				"fmum.failtofindpaintjobinjecttarget",
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
