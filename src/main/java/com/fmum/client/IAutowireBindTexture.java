package com.fmum.client;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IAutowireBindTexture
{
	@SideOnly( Side.CLIENT )
	default void bindTexture( ResourceLocation texture ) {
		FMUMClient.MC.renderEngine.bindTexture( texture );
	}
}
