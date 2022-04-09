package com.fmum.common.paintjob;

import java.util.TreeSet;

import com.fmum.common.FMUM;
import com.fmum.common.type.EnumType;
import com.fmum.common.type.ItemVariant;
import com.fmum.common.type.TypeTextParser.LocalTypeFileParser;

/**
 * External paint jobs allows third party content packs to inject paint jobs that are not in the
 * same content pack
 * 
 * @author Giant_Salted_Fish
 */
public class ExternalPaintjob extends ItemVariant
{
	public static final LocalTypeFileParser<ExternalPaintjob>
		parser = new LocalTypeFileParser<>(ExternalPaintjob.class, ItemVariant.parser);
	static { parser.addKeyword("Target", (s, t) -> t.target = s[1]); }
	
	protected static final TreeSet<ExternalPaintjob> waitInject = new TreeSet<>();
	
	public String target = "unspecified";
	
	public ExternalPaintjob(String name) { super(name); }
	
	@Override
	public void postParse()
	{
		super.postParse();
		
		waitInject.add(this);
	}
	
	@Override
	public void postLoad()
	{
		// Inject this paint job to target type if have not yet
		if(waitInject.size() == 0) return;
		
		for(ExternalPaintjob epj : waitInject)
		{
			TypePaintable paintable = TypePaintable.paintables.get(epj.target);
			if(paintable != null)
				paintable.registerExternalPaintjob(this);
			else FMUM.log.error(
				FMUM.proxy.format(
					"fmum.failtoinjectexpaintjob",
					epj.name,
					epj.target
				)
			);
		}
		waitInject.clear();
	}
	
	@Override
	public EnumType getEnumType() { return EnumType.EX_PAINTJOB; }
}
