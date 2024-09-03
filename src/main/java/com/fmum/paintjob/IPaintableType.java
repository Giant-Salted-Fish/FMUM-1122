package com.fmum.paintjob;

import com.fmum.Registry;

public interface IPaintableType
{
	Registry< IPaintableType > REGISTRY = new Registry<>();
	
	
	String getName();
	
	void injectPaintjob( IPaintjob paintjob );
}
