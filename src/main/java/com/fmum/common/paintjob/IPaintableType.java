package com.fmum.common.paintjob;

import com.fmum.common.Registry;

public interface IPaintableType
{
	Registry< IPaintableType > REGISTRY = new Registry<>( IPaintableType::name );
	
	String name();
	
	void injectPaintjob();
}
