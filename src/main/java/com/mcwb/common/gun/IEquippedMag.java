package com.mcwb.common.gun;

import com.mcwb.common.item.IEquippedItem;
import com.mcwb.common.operation.IOperationController;

public interface IEquippedMag extends IEquippedItem
{
	@Override
	public IMag< ? > item();
	
	public IOperationController pushAmmoController();
	
	public IOperationController popAmmoController();
}
