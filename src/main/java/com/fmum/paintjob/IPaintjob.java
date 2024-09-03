package com.fmum.paintjob;

import com.fmum.render.Texture;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IPaintjob
{
	@SideOnly( Side.CLIENT )
	Texture getTexture();
	
	// TODO: Offer cost of the paintjob?
}
