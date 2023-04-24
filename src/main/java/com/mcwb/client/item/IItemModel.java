package com.mcwb.client.item;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IItemModel< R >
{
	@SideOnly( Side.CLIENT )
	R newRenderer();
}
