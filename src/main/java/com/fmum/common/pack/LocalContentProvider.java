package com.fmum.common.pack;

import java.io.File;

/**
 * Content provider that fetches its source from local disk
 * 
 * @author Giant_Salted_Fish
 */
public abstract class LocalContentProvider implements ContentProvider
{
	/**
	 * Source file on local disk
	 */
	protected final File source;
	
	protected LocalContentProvider(File source)
	{
		this.source = source;
	}
	
	@Override
	public String getSourceName() { return this.source.getName(); }
}
