package com.fmum.common.ammo;

import com.fmum.common.item.IItemType;
import com.fmum.common.meta.IdRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IAmmoType extends IItemType
{
	IdRegistry< IAmmoType > REGISTRY = new IdRegistry<>();
	
	String category();
	
	boolean isCase();
	
	@SideOnly( Side.CLIENT )
	void render();
	
	@SideOnly( Side.CLIENT )
	ResourceLocation texture();
}
