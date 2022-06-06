package com.fmum.common.paintjob;

import java.util.Set;

import com.fmum.common.meta.TypeTextured;
import com.fmum.common.util.LocalAttrParser;

public class TypeExternalPaintjob extends TypeTextured implements MetaExternalPaintjob
{
	public static final LocalAttrParser< TypeExternalPaintjob >
		parser = new LocalAttrParser<>( TypeExternalPaintjob.class, TypeTextured.parser );
	static { parser.addKeyword( "Target", ( s, t ) -> t.injectTarget = s[ 1 ] ); }
	
	public String injectTarget = "unspecified";
	
	public TypeExternalPaintjob( String name ) { super( name ); }
	
	@Override
	public void regisPostInitHandler( Set< Runnable > tasks )
	{
		super.regisPostInitHandler( tasks );
		MetaExternalPaintjob.super.regisPostInitHandler( tasks );
	}
	
	@Override
	public void regisPostLoadHandler( Set< Runnable > tasks )
	{
		super.regisPostLoadHandler( tasks );
		MetaExternalPaintjob.super.regisPostLoadHandler( tasks );
	}
	
	@Override
	public String injectTarget() { return this.injectTarget; }
}
