package com.fmum.common.ammo;

import com.fmum.common.IDRegistry;
import com.fmum.common.item.IItemType;
import com.fmum.util.Category;

public interface IAmmoType extends IItemType
{
	IDRegistry< IAmmoType > REGISTRY = new IDRegistry<>( IAmmoType::name );
	
	Category category();
	
	/**
	 * This is called right before firing this round. Can be used to create misfire rounds randomly.
	 */
//	IAmmoType onShoot();  // TODO: Maybe pass in some "IShooter" in as reference.
	
	boolean canShoot();
}
