package com.fmum.common.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fmum.common.FMUM;
import com.fmum.common.meta.MetaBase;
import com.fmum.common.module.MetaModular;
import com.fmum.common.module.ModuleSlot;
import com.fmum.common.module.PreInstalledModules;
import com.fmum.common.module.PreInstalledOnRail;
import com.fmum.common.module.RailSlot;
import com.fmum.common.pack.TypeParser;
import com.fmum.common.paintjob.MetaPaintable;

/**
 * Base for common {@link FMUM} items. They are modular and support paintjob.
 * 
 * @author Giant_Salted_Fish
 */
public abstract class TypeItemCustomizable extends TypeItem implements MetaModular, MetaPaintable
{
	public static final TypeParser< TypeItemCustomizable >
		parser = new TypeParser<>( TypeItem.parser );
	static
	{
		parser.addKeyword( "Slots", ( s, t ) -> t.slots = RailSlot.parse( s, 1 ) );
		parser.addKeyword(
			"DefModules",
			( s, t ) -> {
				PreInstalledOnRail modules = new PreInstalledOnRail( t.name );
				modules.parse( s, 1 );
				t.defModules = modules;
			}
		);
		
		// TODO: hit box
		
	}
	
	public ModuleSlot[] slots = DEF_SLOTS;
	
	public PreInstalledModules defModules = DEF_DEF_MODULES;
	
	// TODO: maybe a better initial capacity?
	public ArrayList< MetaBase > paintjobs = new ArrayList<>( 1 );
	
	public TypeItemCustomizable( String name )
	{
		super( name );
		
		// Add itself as a primary paintjob
		this.paintjobs.add( this );
	}
	
	@Override
	public void regisPostInitHandler( Map< String, Runnable > tasks )
	{
		super.regisPostInitHandler( tasks );
		MetaModular.super.regisPostInitHandler( tasks );
		MetaPaintable.super.regisPostInitHandler( tasks );
	}
	
	@Override
	public void regisPostLoadHandler( Map< String, Runnable > tasks )
	{
		super.regisPostLoadHandler( tasks );
		MetaModular.super.regisPostLoadHandler( tasks );
		MetaPaintable.super.regisPostLoadHandler( tasks );
	}
	
	@Override
	public List< MetaBase > paintjobs() { return this.paintjobs; }
}
