package com.mcwb.common.paintjob;

import com.mcwb.common.meta.IMeta;
import com.mcwb.common.meta.Registry;

public interface IPaintableType extends IMeta
{
	public static final Registry< IPaintableType > REGISTRY = new Registry<>();
	
//	public IPaintable getContexted( ICapabilityProvider provider );
	
	/**
	 * Implement this to accept paintjob injection
	 */
	public void injectPaintjob( IPaintjob paintjob );
}
