package com.fmum.common.paintjob;

import java.util.Map;

import com.fmum.common.meta.TypeTextured;
import com.fmum.common.pack.TypeParser;

public class TypeExternalPaintjob extends TypeTextured implements MetaExternalPaintjob
{
	public static final TypeParser< TypeExternalPaintjob >
		parser = new TypeParser<>( TypeExternalPaintjob.class, TypeTextured.parser );
	static { parser.addKeyword( "Target", ( s, t ) -> t.injectTarget = s[ 1 ] ); }
	
	public String injectTarget = "unspecified";
	
	public TypeExternalPaintjob( String name ) { super( name ); }
	
	@Override
	public void regisPostInitHandler( Map< String, Runnable > tasks )
	{
		super.regisPostInitHandler( tasks );
		MetaExternalPaintjob.super.regisPostInitHandler( tasks );
	}
	
	@Override
	public void regisPostLoadHandler( Map< String, Runnable > tasks )
	{
		super.regisPostLoadHandler( tasks );
		MetaExternalPaintjob.super.regisPostLoadHandler( tasks );
	}
	
	@Override
	public String injectTarget() { return this.injectTarget; }
}
