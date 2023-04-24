package com.mcwb.common.ammo;

import com.mcwb.common.item.IItemType;
import com.mcwb.common.meta.Registry;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IAmmoType extends IItemType
{
	static final Registry< IAmmoType > REGISTRY = new Registry<>();
	
	String category();
	
	boolean isCase();
	
	@SideOnly( Side.CLIENT )
	void render();
	
	@SideOnly( Side.CLIENT )
	ResourceLocation texture();
}
