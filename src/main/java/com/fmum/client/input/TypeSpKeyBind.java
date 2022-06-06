package com.fmum.client.input;

import java.util.Set;

import com.fmum.common.util.LocalAttrParser;

public class TypeSpKeyBind extends TypeKeyBind
{
	public static final LocalAttrParser< TypeSpKeyBind >
		parser = new LocalAttrParser<>( TypeSpKeyBind.class, TypeKeyBind.parser );
	static { parser.addKeyword( "UpdateStrategy", ( s, t ) -> t.updateStrategy = s[ 1 ] ); }
	
	public String updateStrategy = "GLOBAL";
	
	public TypeSpKeyBind( String name ) { super( name ); }
	
	@Override
	public void regisPostInitHandler( Set< Runnable > tasks )
	{
		super.regisPostInitHandler( tasks );
		
		tasks.add( () -> InputHandler.regis( this.updateStrategy, this ) );
	}
}
