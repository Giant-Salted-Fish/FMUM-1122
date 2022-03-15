package com.fmum.common.pack;

public interface FMUMContentProvider
{
	/**
	 * Prepare resources for the later loading. Register class path, assist locations.
	 */
	public void prepareLoad();
	
	/**
	 * Load contents in this content provider. Called after {@link #prepareLoad()}.
	 */
	public void loadContents();
	
	/**
	 * @return Name of source where this content provider fetches content from
	 */
	public String getSourceName();
}
