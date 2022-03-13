package com.fmum.common.type;

/**
 * Item types supported by {@link com.fmum.common.FMUM} frame
 * 
 * @author Giant_Salted_Fish
 */
public enum EnumType
{
	GUN("gun"),
	ATTACHMENT("attachment"),
	MAG("mag"),
	BULLET("bullet");
	
	public final String recommendedSourceDirName;
	
	private EnumType(String recommendedSourceDirName)
	{
		this.recommendedSourceDirName = recommendedSourceDirName;
	}
}
