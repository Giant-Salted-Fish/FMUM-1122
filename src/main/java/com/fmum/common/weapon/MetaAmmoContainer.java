package com.fmum.common.weapon;

import java.util.Set;

import com.fmum.common.meta.MetaGrouped;

public interface MetaAmmoContainer extends MetaGrouped
{
	@Override
	public default void regisPostInitHandler( Set< Runnable > tasks ) {
		MetaGrouped.super.regisPostInitHandler( tasks );
	}
	
	@Override
	public default void regisPostLoadHandler( Set< Runnable > tasks ) {
		MetaGrouped.super.regisPostLoadHandler( tasks );
	}
	
	/**
	 * @return Capacity of this ammo container
	 */
	public default int capacity() { return 1; }
	
	/**
	 * @param ammo Ammo to validate
	 * @return {@code true} if the given ammo can be loaded into this container
	 */
	public default boolean isCompatibleAmmo( MetaAmmo ammo ) { return false; }
}
