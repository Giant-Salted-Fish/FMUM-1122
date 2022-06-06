package com.fmum.common.meta;

import java.util.Set;

import com.fmum.client.ResourceHandler;
import com.fmum.common.util.LocalAttrParser;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TypeTextured extends TypeBase
{
	public static final LocalAttrParser< TypeTextured >
		parser = new LocalAttrParser<>( TypeBase.parser );
	static { parser.addKeyword( "Texture", ( s, t ) -> t.texture = s[ 1 ] ); }
	
	/**
	 * Texture of the meta
	 */
	public String texture = null;
	
	public TypeTextured( String name ) { super( name ); }
	
	@Override
	public void regisPostInitHandler( Set< Runnable > tasks )
	{
		super.regisPostInitHandler( tasks );
		
		// Set a default texture if does not have
		tasks.add( () -> {
			if( this.texture == null )
				this.texture = ResourceHandler.TEXTURE_GREEN.getPath();
		} );
	}
	
	@Override
	public void regisPostLoadHandler( Set< Runnable > tasks ) {
		super.regisPostLoadHandler( tasks );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public ResourceLocation texture() { return ResourceHandler.getTexture( this.texture ); }
}
