package com.fmum.common.module;

import com.fmum.common.IDRegistry;

public interface ModuleType
{
	IDRegistry< ModuleType > REGISTRY = new IDRegistry<>( ModuleType::name );
	
	String name();
}
