package com.fmum.common.gun;

public class ItemMag extends ItemAmmoContainer
{
	public final TypeMag type;
	
	public ItemMag(TypeMag type) { this.type = type; }
	
	@Override
	public TypeMag getType() { return this.type; }
}
