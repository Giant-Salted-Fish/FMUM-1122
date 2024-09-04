package com.fmum.ammo;

import com.fmum.BiRegistry;
import com.fmum.item.IItemType;

public interface IAmmoType extends IItemType
{
	BiRegistry< Short, IAmmoType > REGISTRY = BiRegistry.createWithShortKey();
	
	
	String getCategory();
	
	boolean canShoot();
	
	IAmmoType prepareShoot();
}
