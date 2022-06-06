package com.fmum.client;

import java.util.HashMap;

import com.fmum.common.FMUM;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public abstract class ResourceHandler
{
	private static final HashMap< String, ResourceLocation > textures = new HashMap<>();
	
	public static final String RECOMMENDED_TEXTURE_FOLDER = "skins/";
	
	public static final ResourceLocation
		TEXTURE_RED = getTexture( getTexturePath( "0xff0000" ) ),
		TEXTURE_GREEN = getTexture( getTexturePath( "0x00ff00" ) ),
		TEXTURE_BLUE = getTexture( getTexturePath( "0x0000ff" ) );
	
	private ResourceHandler() { }
	
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
	public static String getTexturePath( String name ) {
		return RECOMMENDED_TEXTURE_FOLDER + name + ".png";
	}
}
