package com.fmum.ammo;

import com.fmum.IDRegistry;
import com.fmum.item.IItemType;

public interface IAmmoType extends IItemType
{
	IDRegistry< IAmmoType > REGISTRY = new IDRegistry<>();
	
	
	String getCategory();
	
	boolean canShoot();
	
	IAmmoType prepareShoot();
}
