package com.fmum.common.pack;

import com.fmum.common.Meta;

public interface ContentProvider extends Meta
{
	/**
	 * Prepare resources for the later loading. Register class path, assist locations.
	 */
	public default void prepareLoad() { }
	
	/**
	 * Load contents in this content provider. Called after {@link #prepareLoad()}.
	 */
	public default void loadContent() { }
	
	/**
	 * @return
	 *     Name of source where this content provider fetches content from. Usually is the name of
	 *     the .zip|jar file or folder of the pack.
	 */
	public default String sourceName() { return "unknown"; }
}
