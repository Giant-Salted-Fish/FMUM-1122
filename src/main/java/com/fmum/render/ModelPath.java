package com.fmum.render;

import com.fmum.FMUM;
import gsf.util.render.Mesh;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public final class ModelPath extends ResourceLocation
{
	/**
	 * Correspond to {@link Mesh#NONE}.
	 */
	public static final ModelPath NONE = new ModelPath( FMUM.MODID, "none" );
	
	
	public ModelPath( String identifier ) {
		super( identifier );
	}
	
	public ModelPath( String namespace, String path ) {
		super( namespace, path );
	}
	
	public ModelPath( ResourceLocation resource ) {
		super( resource.getNamespace(), resource.getPath() );
	}
}