package com.fmum.common.gun;

import com.fmum.common.type.EnumType;
import com.fmum.common.type.TypeTextParser.LocalTypeFileParser;

import net.minecraft.item.Item;

/**
 * Type of the magazine that can be loaded into a gun
 * 
 * @author Giant_Salted_Fish
 */
public class TypeMag extends TypeAmmoContainer
{
	public static final LocalTypeFileParser<TypeMag>
		parser = new LocalTypeFileParser<>(TypeMag.class, TypeAmmoContainer.parser);
	static
	{
		
	}
	
	public TypeMag(String name) { super(name); }
	
	@Override
	public Item getRegistrantItem() { return this.withItem(new ItemMag(this)); }
	
	@Override
	public EnumType getEnumType() { return EnumType.MAG; }
}
