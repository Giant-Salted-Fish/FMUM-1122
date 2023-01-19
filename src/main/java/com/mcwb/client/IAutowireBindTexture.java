package com.mcwb.client;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public interface IAutowireBindTexture
{
	public default void bindTexture( ResourceLocation texture ) {
		MCWBClient.MC.renderEngine.bindTexture( texture );
	}
}
