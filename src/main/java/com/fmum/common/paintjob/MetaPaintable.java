package com.fmum.common.paintjob;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.fmum.common.meta.MetaBase;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Meta that inherits this interface will could have multiple skins(textures) that provided by
 * {@link MetaPaintjob}. It also supports third party paintjob injection.
 * into this meta.
 * 
 * @see MetaPaintjob
 * @see MetaExternalPaintjob
 * @author Giant_Salted_Fish
 */
public interface MetaPaintable extends MetaBase
{
	public static final HashMap< String, MetaPaintable > regis= new HashMap<>();
	
	@Override
	public default void regisPostInitHandler( Map< String, Runnable > tasks )
	{
		MetaBase.super.regisPostInitHandler( tasks );
		
		tasks.put( "REGIS_PAINTABLE", () -> this.regisTo( this, regis ) );
	}
	
	@Override
	public default void regisPostLoadHandler( Map< String, Runnable > tasks ) {
		MetaBase.super.regisPostLoadHandler( tasks );
	}
	
	/**
	 * Override this method if you want to do a check before adding the paint job or reject any
	 * paintjob adding
	 * 
	 * @param paintjob Paint job to add
	 */
	public default void injectPaintjob( MetaBase paintjob )
	{
		List< MetaBase > paintjobs = this.paintjobs();
		paintjobs.add( paintjob );
		paintjobs.sort( null );
	}
	
	/**
	 * This method is only for convenience. If your implementation uses a list to store all
	 * paintjobs then you can override this method to let the default methods to automatically
	 * complete the injection of the external paintjobs. In runtime circumstance you should not
	 * assume that this method always work for any implementation.
	 * 
	 * @note
	 *     If you use a special data structure to store the paintjob then you also need to override
	 *     {@link #injectPaintjob(MetaPaintjob)} to handle the paintjob inject request yourself
	 * @return A list that contains all paintjobs if available
	 */
	@Nullable
	public default List< MetaBase > paintjobs() { return null; }
	
	@SideOnly( Side.CLIENT )
	public default ResourceLocation texture( int i ) { return this.paintjobs().get( i ).texture(); }
	
	public static MetaPaintable get( String name ) { return regis.get( name ); }
}
