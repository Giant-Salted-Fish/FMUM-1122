package com.fmum.client.input;

import java.util.Map;

import com.fmum.common.pack.TypeParser;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class TypeSpKeyBind extends TypeKeyBind
{
	public static final TypeParser< TypeSpKeyBind >
		parser = new TypeParser<>( TypeSpKeyBind.class, TypeKeyBind.parser );
	static { parser.addKeyword( "UpdateStrategy", ( s, t ) -> t.updateStrategy = s[ 1 ] ); }
	
	public String updateStrategy = "GLOBAL";
	
	public TypeSpKeyBind( String name ) { super( name ); }
	
	@Override
	public void regisPostInitHandler( Map< String, Runnable > tasks )
	{
		super.regisPostInitHandler( tasks );
		
		tasks.put( "LOC_UPDATE_CHANNEL", () -> InputHandler.regis( this.updateStrategy, this ) );
	}
}
