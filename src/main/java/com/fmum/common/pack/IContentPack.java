package com.fmum.common.pack;

public interface IContentPack
{
	String name();
	
	String author();
	
	/**
	 * Used in error handling procedure to give human-readable names that can be used to identify
	 * problems.
	 */
	String sourceName();
}
