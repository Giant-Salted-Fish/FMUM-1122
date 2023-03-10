package com.mcwb.common.gun;

import com.mcwb.common.item.IInUseItem;
import com.mcwb.common.operation.IOperationController;

public interface IInUseMag extends IInUseItem
{
	@Override
	public IMag< ? > using();
	
	public IOperationController pushAmmoController();
	
	public IOperationController popAmmoController();
}
