package com.fmum.common.paintjob;

import com.fmum.common.meta.IMeta;
import com.fmum.common.meta.Registry;

public interface IPaintableType extends IMeta
{
	Registry< IPaintableType > REGISTRY = new Registry<>();
	
	/**
	 * Implement this to accept paintjob injection.
	 */
	void injectPaintjob( IPaintjob paintjob );
}
