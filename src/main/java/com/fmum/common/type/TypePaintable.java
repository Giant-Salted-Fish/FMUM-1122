package com.fmum.common.type;

import com.fmum.common.type.TypeTextParser.LocalTypeFileParser;

public abstract class TypePaintable extends TypeInfo
{
	public static final LocalTypeFileParser<TypePaintable>
		parser = new LocalTypeFileParser<>(TypeInfo.parser);
	static
	{
		parser.addKeyword("Paintjob", (s, t) -> { }); // TODO
	}
	
	protected TypePaintable(String name, String contentPackName) { super(name, contentPackName); }
	
	public static final class Paintjob
	{
		
	}
}
