package com.fmum.common.gun;

import java.util.HashMap;

import com.fmum.common.type.EnumType;
import com.fmum.common.type.TypeTextParser.LocalTypeFileParser;

public class TypeAttachment extends TypeGunPart
{
	public static final HashMap<String, TypeAttachment> attachments = new HashMap<>();
	
	public static final LocalTypeFileParser<TypeAttachment>
		parser = new LocalTypeFileParser<TypeAttachment>(TypeAttachment.class, TypeGunPart.parser);
	
	public TypeAttachment(String name) { super(name); }
	
	@Override
	public void postParse()
	{
		super.postParse();
		
		attachments.put(this.name, this);
	}
	
	@Override
	protected void onItemSetup() { this.withItem(new ItemAttachment(this)); }
	
	@Override
	public EnumType getEnumType() { return EnumType.ATTACHMENT; }
}
