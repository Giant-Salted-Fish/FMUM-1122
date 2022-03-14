package com.fmum.common.type;

import java.util.HashMap;

public abstract class TypeInfo
{
	public static final HashMap<String, TypeInfo> types = new HashMap<>();
	
	public String name;
	
	public String contentPackName;
	
	public void register()
	{
		types.put(this.name, this);
	}
	
	/**
	 * @return {@link EnumType} that this instance belongs to
	 */
	public abstract EnumType getEnumType();
}
