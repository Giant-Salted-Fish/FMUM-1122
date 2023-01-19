package com.mcwb.common.paintjob;

import com.mcwb.common.meta.IMeta;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// TODO: mayebe let it be able to change model?
public interface IPaintjob extends IMeta
{
	@SideOnly( Side.CLIENT )
	public ResourceLocation texture();
}
