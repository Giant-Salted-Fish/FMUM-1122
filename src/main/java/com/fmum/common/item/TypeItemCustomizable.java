package com.fmum.common.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fmum.common.FMUM;
import com.fmum.common.meta.MetaBase;
import com.fmum.common.module.MetaModular;
import com.fmum.common.paintjob.MetaPaintable;
import com.fmum.common.util.LocalAttrParser;

/**
 * Base for common {@link FMUM} items. They are modular and can be painted.
 * 
 * @author Giant_Salted_Fish
 */
public abstract class TypeItemCustomizable extends TypeItem implements MetaModular, MetaPaintable
{
	public static final LocalAttrParser< TypeItemCustomizable >
		parser = new LocalAttrParser<>( TypeItem.parser );
	static
	{
		
	}
	
	// TODO: maybe a better initial capacity?
	public ArrayList< MetaBase > paintjobs = new ArrayList<>( 1 );
	
	public TypeItemCustomizable( String name )
	{
		super( name );
		
		// Add itself as a primary paintjob
		this.paintjobs.add( this );
	}
	
	@Override
	public void regisPostInitHandler( Set< Runnable > tasks )
	{
		super.regisPostInitHandler( tasks );
		MetaModular.super.regisPostInitHandler( tasks );
		MetaPaintable.super.regisPostInitHandler( tasks );
	}
	
	@Override
	public void regisPostLoadHandler( Set< Runnable > tasks )
	{
		super.regisPostLoadHandler( tasks );
		MetaModular.super.regisPostLoadHandler( tasks );
		MetaPaintable.super.regisPostLoadHandler( tasks );
	}
	
	@Override
	public List< MetaBase > paintjobs() { return this.paintjobs; }
}
