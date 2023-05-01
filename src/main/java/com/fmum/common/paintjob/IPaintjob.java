package com.fmum.common.paintjob;

import com.fmum.common.meta.IMeta;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IPaintjob extends IMeta
{
	@SideOnly( Side.CLIENT )
	ResourceLocation texture();
}
