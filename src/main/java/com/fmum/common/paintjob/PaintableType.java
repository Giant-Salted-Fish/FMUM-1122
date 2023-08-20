package com.fmum.common.paintjob;

import com.fmum.common.Registry;

public interface PaintableType
{
	Registry< PaintableType > REGISTRY = new Registry<>( PaintableType::name );
	
	String name();
	
	void injectPaintjob( Paintjob paintjob );
}
