package com.fmum.common.type;

import com.fmum.common.type.TypeTextParser.LocalTypeFileParser;

/**
 * Item types supported by {@link com.fmum.common.FMUM} frame
 * 
 * @author Giant_Salted_Fish
 */
public enum EnumType
{
	GUN("gun", null),
	ATTACHMENT("attachment", null),
	MAG("mag", null),
	BULLET("bullet", null);
	
	public static final String RECOMMENDED_TAB_SOURCE_DIR_NAME = "tab";
	
	public final String recommendedSourceDirName;
	
	public final LocalTypeFileParser<? extends TypeInfo> parser;
	
	private EnumType(String recommendedSourceDirName, LocalTypeFileParser<?> parser)
	{
		this.recommendedSourceDirName = recommendedSourceDirName;
		this.parser = parser;
	}
}
