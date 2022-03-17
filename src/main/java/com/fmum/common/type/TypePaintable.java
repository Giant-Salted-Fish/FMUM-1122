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
	
	protected TypePaintable(String name) { super(name); }
	
	public static final class Paintjob
	{
		// TODO: do not forget to localize paintjob name server side
	}
}
