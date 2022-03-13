package com.fmum.common.type;

public abstract class TypeInfo
{
	public String name;
	
	public String contentPackName;
	
	/**
	 * @return {@link EnumType} that this instance belongs to
	 */
	public abstract EnumType getEnumType();
}
