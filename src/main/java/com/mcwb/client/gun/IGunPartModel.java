package com.mcwb.client.gun;

import com.mcwb.client.item.IItemModel;
import com.mcwb.client.modify.IModifiableModel;
import com.mcwb.common.gun.IContextedGunPart;

public interface IGunPartModel< T extends IContextedGunPart >
	extends IItemModel< T >, IModifiableModel< T >
{
	
}
