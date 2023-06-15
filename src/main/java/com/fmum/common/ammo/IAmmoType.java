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
	
	/**
	 * This is called right before firing this round. Can be used to create misfire rounds randomly.
	 */
	IAmmoType onShoot(); // TODO: Maybe pass in some "IShooter" in as reference.
	
	boolean isShootable();
	
	@SideOnly( Side.CLIENT )
	void render();
	
	@SideOnly( Side.CLIENT )
	ResourceLocation texture();
}
