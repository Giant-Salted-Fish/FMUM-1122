package com.fmum.client;

import java.util.HashMap;

import com.fmum.common.FMUM;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class ResourceManager
{
	private static final HashMap<String, ResourceLocation> textures = new HashMap<>();
	
	public static final String RECOMMENDED_TEXTURE_FOLDER = "skins/";
	
	public static final ResourceLocation
		TEXTURE_RED = getTexture(RECOMMENDED_TEXTURE_FOLDER + "0xff0000.png"),
		TEXTURE_GREEN = getTexture(RECOMMENDED_TEXTURE_FOLDER + "0x00ff00.png"),
		TEXTURE_BLUE = getTexture(RECOMMENDED_TEXTURE_FOLDER + "0x0000ff.png");
	
	private ResourceManager() { }
	
	public static ResourceLocation getTexture(String path)
	{
		ResourceLocation loc = textures.get(path);
		if(loc == null)
			textures.put(
				path,
				loc = new ResourceLocation(FMUM.MODID, path)
			);
		return loc;
	}
}
