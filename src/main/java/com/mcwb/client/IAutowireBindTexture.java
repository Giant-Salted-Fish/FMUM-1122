package com.mcwb.client;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IAutowireBindTexture
{
	@SideOnly( Side.CLIENT )
	default void bindTexture( ResourceLocation texture ) {
		MCWBClient.MC.renderEngine.bindTexture( texture );
	}
}
