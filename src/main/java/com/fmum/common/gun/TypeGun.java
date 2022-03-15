package com.fmum.common.gun;

import java.util.HashMap;

import com.fmum.common.type.EnumType;

import net.minecraft.item.Item;

public final class TypeGun extends TypeMag
{
	public static final HashMap<String, TypeGun> guns = new HashMap<>();
	
	protected TypeGun(String name, String contentPackName) { super(name, contentPackName); }
	
	@Override
	public Item getRegistrantItem() { return this.withItem(new ItemGun(this)); }
	
	@Override
	public EnumType getEnumType() { return EnumType.GUN; }
}
