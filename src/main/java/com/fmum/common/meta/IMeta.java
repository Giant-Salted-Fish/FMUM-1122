package com.fmum.common.meta;

import com.fmum.common.FMUM;

/**
 * Root interface for all elements managed by {@link FMUM}.
 * 
 * @author Giant_Salted_Fish
 */
@FunctionalInterface
public interface IMeta
{
	/**
	 * @return Name of the meta. PS: By my definition, name is essentially what defines a meta.
	 */
	String name();
}
