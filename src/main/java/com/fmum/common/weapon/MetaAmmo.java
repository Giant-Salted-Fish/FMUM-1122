package com.fmum.common.weapon;

import java.util.Set;

import com.fmum.common.meta.MetaGrouped;

/**
 * Basic abstract all the ammo, including various bullets and grenades.
 * 
 * @author Giant_Salted_Fish
 */
public interface MetaAmmo extends MetaGrouped
{
	@Override
	public default void regisPostInitHandler( Set< Runnable > tasks ) {
		MetaGrouped.super.regisPostInitHandler( tasks );
	}
	
	@Override
	public default void regisPostLoadHandler( Set< Runnable > tasks ) {
		MetaGrouped.super.regisPostLoadHandler( tasks );
	}
}
