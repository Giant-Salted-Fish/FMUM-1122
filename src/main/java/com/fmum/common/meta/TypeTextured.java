package com.fmum.common.meta;

import java.util.Set;

import com.fmum.common.FMUM;
import com.fmum.common.util.LocalAttrParser;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TypeTextured extends TypeBase
{
	public static final LocalAttrParser< TypeTextured >
		parser = new LocalAttrParser<>( TypeBase.parser );
	static
	{
		parser.addKeyword( "ModelScale", ( s, t ) -> t.modelScale = Double.parseDouble( s[ 1 ] ) );
		parser.addKeyword( "Texture", ( s, t ) -> t.texture = FMUM.MOD.loadTexture( s[ 1 ] ) );
	}
	
	public double modelScale = 1D;
	
	public ResourceLocation texture = FMUM.MOD.loadTexture( "skins/0x00ff00.png" );
	
	public TypeTextured( String name ) { super( name ); }
	
	@Override
	public void regisPostLoadHandler( Set< Runnable > tasks ) {
		super.regisPostLoadHandler( tasks );
	}
	
	@Override
	public double modelScale() { return this.modelScale; }
	
	@Override
	@SideOnly( Side.CLIENT )
	public ResourceLocation texture() { return this.texture; }
}
