package com.fmum.common.gun;

import java.util.HashMap;
import java.util.TreeSet;

import com.fmum.common.FMUM;
import com.fmum.common.type.EnumType;
import com.fmum.common.type.TypeInfo;
import com.fmum.common.type.TypeTextParser.LocalTypeFileParser;

import net.minecraft.item.Item;

public final class TypeGun extends TypeAmmoContainer
{
	public static final HashMap<String, TypeGun> guns = new HashMap<>();
	
	public static final LocalTypeFileParser<TypeGun>
		parser = new LocalTypeFileParser<>(TypeGun.class, TypeAmmoContainer.parser);
	static
	{
		parser.addKeyword(
			"Mag",
			(s, t) -> {
				if(t.mags == FMUM.EMPTY_STR_SET)
					t.mags = new TreeSet<>();
				for(int i = s.length; --i > 0; t.mags.add(s[i]));
			}
		);
	}
	
	public TreeSet<String> mags = FMUM.EMPTY_STR_SET;
	
	public TypeGun(String name) { super(name); }
	
	@Override
	public Item getRegistrantItem() { return this.withItem(new ItemGun(this)); }
	
	@Override
	public EnumType getEnumType() { return EnumType.GUN; }
	
	@Override
	public final boolean validateMag(TypeInfo mag) { return this.mags.contains(mag.category); }
}
