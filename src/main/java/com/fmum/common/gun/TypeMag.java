package com.fmum.common.gun;

import java.util.HashMap;

import com.fmum.common.type.EnumType;
import com.fmum.common.type.TypeTextParser.LocalTypeFileParser;

/**
 * Type of the magazine that can be loaded into a gun
 * 
 * @author Giant_Salted_Fish
 */
public class TypeMag extends TypeAmmoContainer
{
	public static final HashMap<String, TypeMag> mags = new HashMap<>();
	
	public static final LocalTypeFileParser<TypeMag>
		parser = new LocalTypeFileParser<>(TypeMag.class, TypeAmmoContainer.parser);
	static
	{
		
	}
	
	public TypeMag(String name) { super(name); }
	
	@Override
	public void postParse()
	{
		super.postParse();
		
		mags.put(this.name, this);
	}
	
	@Override
	public void onItemSetup() { this.withItem(new ItemMag(this)); }
	
	@Override
	public EnumType getEnumType() { return EnumType.MAG; }
}
