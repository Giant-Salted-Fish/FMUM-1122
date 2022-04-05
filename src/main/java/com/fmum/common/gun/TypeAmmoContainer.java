package com.fmum.common.gun;

import java.util.TreeSet;

import com.fmum.common.type.TypeInfo;
import com.fmum.common.type.TypeTextParser.LocalTypeFileParser;

public abstract class TypeAmmoContainer extends TypeGunPart
{
	public static final LocalTypeFileParser<TypeAmmoContainer>
		parser = new LocalTypeFileParser<>(TypeGunPart.parser);
	static
	{
		parser.addKeyword("AmmoCapacity", (s, t) -> t.ammoCapacity = Integer.parseInt(s[1]));
		parser.addKeyword(
			"Ammo",
			(s, t) -> {
				if(t.ammo == TypeInfo.EMPTY_STR_SET)
					t.ammo = new TreeSet<>();
				for(int i = s.length; --i > 0; t.ammo.add(s[i]));
			}
		);
	}
	
	/**
	 * How much ammo can be loaded into this container
	 */
	public int ammoCapacity = 1;
	
	/**
	 * All ammo types that accepted by this ammo container
	 */
	public TreeSet<String> ammo = TypeInfo.EMPTY_STR_SET;
	
	protected TypeAmmoContainer(String name) { super(name); }
	
	@Override
	public final int getAmmoCapacity() { return this.ammoCapacity; }
	
	@Override
	public final boolean validateAmmo(TypeInfo ammo) { return this.ammo.contains(ammo.category); }
}
