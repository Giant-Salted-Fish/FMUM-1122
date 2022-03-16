package com.fmum.common.gun;

import com.fmum.common.module.TypeModule;
import com.fmum.common.type.EnumType;
import com.fmum.common.type.TypeTextParser.LocalTypeFileParser;

import net.minecraft.item.Item;

public class TypeMag extends TypeModule
{
	public static final LocalTypeFileParser<TypeMag>
		parser = new LocalTypeFileParser<>(TypeMag.class, TypeModule.parser);
	static
	{
		
	}
	
	public TypeMag(String name, String contentPackName) {
		super(name, contentPackName);
	}
	
	@Override
	public Item getRegistrantItem() { return this.withItem(new ItemMag(this)); }
	
	@Override
	public EnumType getEnumType() { return EnumType.MAG; }
}
