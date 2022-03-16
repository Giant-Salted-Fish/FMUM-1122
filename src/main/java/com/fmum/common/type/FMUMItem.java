package com.fmum.common.type;

/**
 * Base item type for all items accepted by FMUM
 * 
 * @author Giant_Salted_Fish
 */
public interface FMUMItem
{
	/**
	 * @return Name of this item. Usually can be used to identify different items in FMUM.
	 */
	public String getName();
	
	/**
	 * @return Name of the content pack that this item belongs to
	 */
	public String getContentPackName();
	
	public TypeInfo getType();
}
