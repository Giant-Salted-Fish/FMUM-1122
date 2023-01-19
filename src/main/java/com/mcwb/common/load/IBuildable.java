package com.mcwb.common.load;

import com.mcwb.common.pack.IContentProvider;

@FunctionalInterface
public interface IBuildable< T >
{
	/**
	 * @param name An alternative name that usually obtained from its file name
	 * @param provider Content pack that provides this meta
	 */
	public T build( String name, IContentProvider provider );
}
