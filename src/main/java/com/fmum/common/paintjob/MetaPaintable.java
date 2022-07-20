package com.fmum.common.paintjob;

import java.util.HashMap;
import java.util.Map;

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
	 * Implement this to accept an {@link MetaExternalPaintjob}
	 * 
	 * @param paintjob Paint job to be injected
	 */
	public void injectPaintjob( MetaBase paintjob );
	
	public int numTextures();
	
	@SideOnly( Side.CLIENT )
	public ResourceLocation texture( int idx );
	
	public static MetaPaintable get( String name ) { return regis.get( name ); }
}
