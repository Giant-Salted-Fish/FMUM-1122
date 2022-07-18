package com.fmum.client;

import java.util.HashMap;

import com.fmum.common.FMUM;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public abstract class ResourceManager
{
	private static final HashMap< String, ResourceLocation > textures = new HashMap<>();
	
	public static final String RECOMMENDED_TEXTURE_FOLDER = "skins/";
	
	public static final ResourceLocation
		TEXTURE_RED = getTexture( formatTexturePath( "0xff0000" ) ),
		TEXTURE_GREEN = getTexture( formatTexturePath( "0x00ff00" ) ),
		TEXTURE_BLUE = getTexture( formatTexturePath( "0x0000ff" ) );
	
	private ResourceManager() { }
	
	public static ResourceLocation getTexture( String path )
	{
		ResourceLocation loc = textures.get( path );
		if( loc == null )
			textures.put(
				path,
				loc = new ResourceLocation( FMUM.MODID, path )
			);
		return loc;
	}
	
	/**
	 * @return Name with {@value #RECOMMENDED_TEXTURE_FOLDER} in front of and ".png" behind
	 */
	public static String formatTexturePath( String name ) {
		return RECOMMENDED_TEXTURE_FOLDER + name + ".png";
	}
}
