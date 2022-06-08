package com.fmum.common.weapon;

import java.util.Set;

import com.fmum.common.meta.MetaBase;

public interface MetaAmmoContainer extends MetaBase
{
	@Override
	public default void regisPostInitHandler( Set< Runnable > tasks ) {
		MetaBase.super.regisPostInitHandler( tasks );
	}
	
	@Override
	public default void regisPostLoadHandler( Set< Runnable > tasks ) {
		MetaBase.super.regisPostLoadHandler( tasks );
	}
	
	/**
	 * @return Capacity of this ammo container
	 */
	public default int ammoCapacity() { return 0; }
	
	/**
	 * @param ammo Ammo to validate
	 * @return {@code true} if the given ammo can be loaded into this container
	 */
	public default boolean validateAmmo( MetaAmmo ammo ) { return false; }
}
