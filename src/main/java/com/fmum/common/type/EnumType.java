package com.fmum.common.type;

import com.fmum.common.gun.TypeAttachment;
import com.fmum.common.gun.TypeGun;
import com.fmum.common.gun.TypeMag;
import com.fmum.common.paintjob.ExternalPaintjob;
import com.fmum.common.type.TypeTextParser.LocalTypeFileParser;

/**
 * Item types supported by {@link com.fmum.common.FMUM} frame. These types should be a sub-type of
 * {@link TypeInfo}.
 * 
 * @author Giant_Salted_Fish
 */
public enum EnumType
{
	GUN("gun", TypeGun.parser),
	ATTACHMENT("attachment", TypeAttachment.parser),
	MAG("mag", TypeMag.parser),
	BULLET("bullet", null),
	EX_PAINTJOB("expaintjob", ExternalPaintjob.parser);
	
	public final String recommendedSourceDirName;
	
	public final LocalTypeFileParser<? extends ItemVariant> parser;
	
	private EnumType(
		String recommendedSourceDirName,
		LocalTypeFileParser<? extends ItemVariant> parser
	) {
		this.recommendedSourceDirName = recommendedSourceDirName;
		this.parser = parser;
	}
}
