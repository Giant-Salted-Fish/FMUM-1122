package com.mcwb.common.paintjob;

import com.mcwb.common.meta.IMeta;
import com.mcwb.common.meta.Registry;

public interface IPaintableType extends IMeta
{
	static final Registry< IPaintableType > REGISTRY = new Registry<>();
	
	/**
	 * Implement this to accept paintjob injection.
	 */
	void injectPaintjob( IPaintjob paintjob );
}
