package com.mcwb.client.gun;

import com.mcwb.client.item.IItemRenderer;
import com.mcwb.client.modify.IModifiableRenderer;
import com.mcwb.common.gun.IContextedGunPart;

public interface IGunPartRenderer< T extends IContextedGunPart >
	extends IItemRenderer< T >, IModifiableRenderer< T >
{
	
}
