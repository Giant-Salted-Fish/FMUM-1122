package com.mcwb.client.ammo;

import com.mcwb.client.item.IItemRenderer;
import com.mcwb.common.meta.IContexted;
import com.mcwb.common.meta.IMeta;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IAmmoRenderer< T extends IMeta, C extends IContexted > extends IItemRenderer< C >
{
	@SideOnly( Side.CLIENT )
	public void render( T type );
}
