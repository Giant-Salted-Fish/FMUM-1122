package com.fmum.common.gun;

import java.util.Map;

/**
 * Magazines that can be used by a gun
 * 
 * @author Giant_Salted_Fish
 */
public interface MetaMag extends MetaGunPart
{
	@Override
	public default void regisPostInitHandler( Map< String, Runnable > tasks ) {
		MetaGunPart.super.regisPostInitHandler( tasks );
	}
	
	@Override
	public default void regisPostLoadHandler( Map< String, Runnable > tasks ) {
		MetaGunPart.super.regisPostLoadHandler( tasks );
	}
}
