package com.mcwb.common.paintjob;

import com.mcwb.common.meta.IContexted;

public interface IPaintable extends IContexted
{
	public int paintjobCount();
	
	public int paintjob();
	
	public void updatePaintjob( int paintjob );
}
