package com.fmum.common.gun;

import java.util.HashMap;
import java.util.Map;

public interface MetaGun extends MetaGunPart
{
	public static final HashMap< String, MetaGun > regis = new HashMap<>();
	
	@Override
	public default void regisPostInitHandler( Map< String, Runnable > tasks )
	{
		MetaGunPart.super.regisPostInitHandler( tasks );
		
		tasks.put( "REGIS_GUN", () -> this.regisTo( this, regis ) );
	}
	
	@Override
	public default void regisPostLoadHandler( Map< String, Runnable > tasks ) {
		MetaGunPart.super.regisPostLoadHandler( tasks );
	}
	
	public static MetaGun get( String name ) { return regis.get( name ); }
}
