package com.fmum.common.meta;

import com.fmum.common.FMUM;
import com.fmum.common.pack.TypeParser;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TypeTextured extends TypeBase
{
	public static final TypeParser< TypeTextured >
		parser = new TypeParser<>( TypeBase.parser );
	static {
		parser.addKeyword( "Texture", ( s, t ) -> t.texture = FMUM.MOD.loadTexture( s[ 1 ] ) );
	}
	
	public ResourceLocation texture = FMUM.MOD.loadTexture( "skins/0x00ff00.png" );
	
	public TypeTextured( String name ) { super( name ); }
	
	@Override
	@SideOnly( Side.CLIENT )
	public ResourceLocation texture() { return this.texture; }
}
