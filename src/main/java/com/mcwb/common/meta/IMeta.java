package com.mcwb.common.meta;

import com.mcwb.common.MCWB;

/**
 * Root interface for all elements managed by {@link MCWB}.
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
