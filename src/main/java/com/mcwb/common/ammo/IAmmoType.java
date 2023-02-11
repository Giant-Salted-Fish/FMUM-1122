package com.mcwb.common.ammo;

import com.mcwb.common.item.IItemType;
import com.mcwb.common.meta.Registry;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IAmmoType extends IItemType
{
	public static final Registry< IAmmoType > REGISTRY = new Registry<>();
	
	public String category();
	
	@SideOnly( Side.CLIENT )
	public void render();
	
	@SideOnly( Side.CLIENT )
	public ResourceLocation texture();
}
