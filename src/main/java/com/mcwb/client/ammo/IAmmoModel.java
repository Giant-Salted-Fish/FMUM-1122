package com.mcwb.client.ammo;

import com.mcwb.client.item.IItemModel;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IAmmoModel< T, R > extends IItemModel< R >
{
	@SideOnly( Side.CLIENT )
	void render( T type );
}
