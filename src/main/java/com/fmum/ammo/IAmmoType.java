package com.fmum.ammo;

import com.fmum.BiRegistry;
import com.fmum.item.IItemType;
import com.fmum.item.ItemCategory;

public interface IAmmoType extends IItemType
{
	BiRegistry< Short, IAmmoType > REGISTRY = BiRegistry.createWithShortKey();
	
	
	ItemCategory getCategory();
	
	boolean canShoot();
	
	IAmmoType prepareShoot();
}
