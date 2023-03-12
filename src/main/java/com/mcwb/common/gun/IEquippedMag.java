package com.mcwb.common.gun;

import com.mcwb.common.item.IEquippedItem;
import com.mcwb.common.operation.IOperationController;

public interface IEquippedMag< T extends IMag< ? > > extends IEquippedItem< T >
{
	public IOperationController pushAmmoController();
	
	public IOperationController popAmmoController();
}
