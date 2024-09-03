package com.fmum.render;

import com.fmum.FMUM;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public final class Texture extends ResourceLocation
{
	public static final Texture RED = new Texture( FMUM.MODID, "textures/0xff0000.png" );
	public static final Texture GREEN = new Texture( FMUM.MODID, "textures/0x00ff00.png" );
	public static final Texture BLUE = new Texture( FMUM.MODID, "textures/0x0000ff.png" );
	
	
	public Texture( String identifier ) {
		super( identifier );
	}
	
	public Texture( String namespace, String path ) {
		super( namespace, path );
	}
	
	public Texture( ResourceLocation resource ) {
		super( resource.getNamespace(), resource.getPath() );
	}
}
