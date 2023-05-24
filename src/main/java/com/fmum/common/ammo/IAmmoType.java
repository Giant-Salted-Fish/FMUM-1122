package com.fmum.common.ammo;

import com.fmum.common.item.IItemType;
import com.fmum.common.meta.Registry;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IAmmoType extends IItemType
{
	Registry< IAmmoType > REGISTRY = new Registry<>();
	
	String category();
	
	boolean isCase();
	
	@SideOnly( Side.CLIENT )
	void render();
	
	@SideOnly( Side.CLIENT )
	ResourceLocation texture();
}
