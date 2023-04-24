package com.mcwb.common.load;

/**
 * @param <T> Type that produced after build.
 * @author Giant_Salted_Fish
 */
@FunctionalInterface
public interface IBuildable< T >
{
	/**
	 * @param name An optional name that usually obtained from its file name.
	 * @param provider Where this buildable is load from.
	 */
	T build( String name, IContentProvider provider );
}
