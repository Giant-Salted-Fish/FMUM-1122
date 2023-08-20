package com.fmum.common.paintjob;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface Paintjob
{
	String name();
	
	@SideOnly( Side.CLIENT )
	ResourceLocation texture();
}
