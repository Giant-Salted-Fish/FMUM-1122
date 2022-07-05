package com.fmum.common.weapon;

import java.util.Map;

import com.fmum.common.meta.MetaGrouped;

/**
 * Basic abstract all the ammo, including various bullets and grenades.
 * 
 * @author Giant_Salted_Fish
 */
public interface MetaAmmo extends MetaGrouped
{
	@Override
	public default void regisPostInitHandler( Map< String, Runnable > tasks ) {
		MetaGrouped.super.regisPostInitHandler( tasks );
	}
	
	@Override
	public default void regisPostLoadHandler( Map< String, Runnable > tasks ) {
		MetaGrouped.super.regisPostLoadHandler( tasks );
	}
}
