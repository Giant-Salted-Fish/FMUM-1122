package com.fmum.common.gun;

import java.util.HashMap;

import com.fmum.common.type.EnumType;
import com.fmum.common.type.TypeTextParser.LocalTypeFileParser;

import net.minecraft.nbt.NBTTagList;

public final class TypeGun extends TypeAmmoContainer
{
	public static final HashMap<String, TypeGun> guns = new HashMap<>();
	
	public static final LocalTypeFileParser<TypeGun>
		parser = new LocalTypeFileParser<>(TypeGun.class, TypeAmmoContainer.parser);
	static
	{
	}
	
	public TypeGun(String name)
	{
		super(name);
		
		// Assign a default hand priority to gun
		this.leftHandPriority
			= this.rightHandPriority
			= -1;
	}
	
	@Override
	public void postParse()
	{
		super.postParse();
		
		guns.put(this.name, this);
	}
	
	@Override
	public NBTTagList genTag(int dam)
	{
		NBTTagList tag = super.genTag(dam);
		
		// TODO: a ton of states based on attachments installed
		
		return tag;
	}
	
	@Override
	public void onItemSetup() { this.withItem(new ItemGun(this)); }
	
	@Override
	public EnumType getEnumType() { return EnumType.GUN; }
	
	@Override
	protected int[] genStates() { return new int[TagGun.NUM_STATES]; }
}
