package com.fmum.module;

import com.fmum.render.Texture;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@FunctionalInterface
@SideOnly( Side.CLIENT )
public interface IModifyContext
{
	Texture mapTexture( Texture texture );
}
