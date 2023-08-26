package com.fmum.common.module;

import com.fmum.common.IDRegistry;

public interface IModuleType
{
	IDRegistry< IModuleType > REGISTRY = new IDRegistry<>( IModuleType::name );
	
	String name();
}
