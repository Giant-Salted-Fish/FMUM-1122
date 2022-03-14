package com.fmum.common.pack;

public interface ContentProvider
{
	/**
	 * Load content from the source
	 */
	public void load();
	
	/**
	 * @return Name of source where this content provider fetches content from
	 */
	public String getSourceName();
}
