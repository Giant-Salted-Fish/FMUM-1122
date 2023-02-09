package com.mcwb.client.gun;

import com.mcwb.client.item.IItemRenderer;
import com.mcwb.client.modify.IModifiableRenderer;
import com.mcwb.common.meta.IContexted;

public interface IGunPartRenderer< T extends IContexted >
	extends IItemRenderer< T >, IModifiableRenderer< T >
{
	
}
