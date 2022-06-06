package com.fmum.common;

import javax.annotation.Nullable;

/**
 * Base of all interfaces in {@link FMUM}. Reserved soft interface for possible future extension.
 * 
 * @author Giant_Salted_Fish
 */
public interface Meta extends Comparable< Meta >
{
	public static final String AUTHOR_MISSING = "fmum.authormissing";
	
	public static final String DESCRIPTION_MISSING = "fmum.descriptionmissing";
	
	/**
	 * @return Name of this meta. Usually can be used to find this meta.
	 */
	public default String name() { return "undefined"; }
	
	/**
	 * @return Author of this meta
	 */
	public default String author() { return AUTHOR_MISSING; }
	
	/**
	 * @return Description of this meta
	 */
	public default String description() { return DESCRIPTION_MISSING; }
	
	/**
	 * Additional information. Reserved soft interface for future extension.
	 * 
	 * @param key
	 * @return Corresponding meta data. {@code null} is not present.
	 */
	@Nullable
	public default Object meta( String key ) { return null; }
	
	@Override
	public default int compareTo( Meta m ) { return this.name().compareTo( m.name() ); }
}
