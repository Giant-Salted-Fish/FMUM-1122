package com.fmum.common;

import net.minecraft.util.ResourceLocation;

/**
 * Quick creation of resources in domain of {@value FMUM#MODID}.
 * 
 * @author Giant_Salted_Fish
 */
public final class FMUMResource extends ResourceLocation
{
	public FMUMResource( String path ) {
		super( FMUM.MODID, path );
	}
}
