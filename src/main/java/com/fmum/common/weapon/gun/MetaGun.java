package com.fmum.common.weapon.gun;

import java.util.HashMap;
import java.util.Set;

public interface MetaGun extends MetaGunPart
{
	public static final HashMap< String, MetaGun > regis = new HashMap<>();
	
	@Override
	public default void regisPostInitHandler( Set< Runnable > tasks )
	{
		MetaGunPart.super.regisPostInitHandler( tasks );
		
		tasks.add( () -> this.regisTo( this, regis ) );
	}
	
	@Override
	public default void regisPostLoadHandler( Set< Runnable > tasks ) {
		MetaGunPart.super.regisPostLoadHandler( tasks );
	}
	
	public static MetaGun get( String name ) { return regis.get( name ); }
}
