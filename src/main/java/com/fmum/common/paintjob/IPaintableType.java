package com.fmum.common.paintjob;

import com.fmum.common.meta.IMeta;
import com.fmum.common.meta.MetaRegistry;

public interface IPaintableType extends IMeta
{
	MetaRegistry< IPaintableType > REGISTRY = new MetaRegistry<>();
	
	/**
	 * Implement this to accept paintjob injection.
	 */
	void injectPaintjob( IPaintjob paintjob );
}
