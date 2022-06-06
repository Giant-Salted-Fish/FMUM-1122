package com.fmum.common.weapon.gun;

import java.util.Set;

/**
 * Magazines that can be used by a gun
 * 
 * @author Giant_Salted_Fish
 */
public interface MetaMag extends MetaGunPart
{
	@Override
	public default void regisPostInitHandler( Set< Runnable > tasks ) {
		MetaGunPart.super.regisPostInitHandler( tasks );
	}
	
	@Override
	public default void regisPostLoadHandler( Set< Runnable > tasks ) {
		MetaGunPart.super.regisPostLoadHandler( tasks );
	}
}
