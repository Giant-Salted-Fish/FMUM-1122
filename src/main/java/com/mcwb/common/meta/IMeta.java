package com.mcwb.common.meta;

import com.mcwb.common.MCWB;

/**
 * Root interface for all elements managed by {@link MCWB}
 * 
 * @author Giant_Salted_Fish
 */
public interface IMeta extends Comparable< IMeta >
{
	/**
	 * @return Name of the meta
	 */
	public String name();
	
	/**
	 * To retrieve additional data from this meta. Reserved for soft extension.
	 * 
	 * @param key Key of the meta to retrieve
	 * @return Corresponding meta data. {@code null} if does not present.
	 */
//	@Nullable
//	public default Object getMeta( Object key ) { return null; }
	
	/**
	 * In default the meta will be compared via their name
	 */
	@Override
	public default int compareTo( IMeta m ) { return this.name().compareTo( m.name() ); }
}
