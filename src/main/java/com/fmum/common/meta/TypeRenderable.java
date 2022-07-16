package com.fmum.common.meta;

import com.fmum.client.render.Renderable;
import com.fmum.common.pack.TypeParser;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TypeRenderable< T extends Renderable > extends TypeTextured
{
	public static final TypeParser< TypeRenderable< ? > >
		parser = new TypeParser<>( TypeTextured.parser );
	
	/**
	 * Model of the renderable. TODO: make sure that this will never be null?
	 */
	@SideOnly( Side.CLIENT )
	protected T model;
	
	public TypeRenderable( String name ) { super( name ); }
}
