package com.fmum.common.module;

import com.fmum.common.IDRegistry;
import net.minecraft.item.ItemStack;

public interface IModuleType
{
	IDRegistry< IModuleType > REGISTRY = new IDRegistry<>( IModuleType::name );
	
	String name();
}
