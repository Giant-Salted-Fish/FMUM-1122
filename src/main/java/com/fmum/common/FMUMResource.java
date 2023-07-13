package com.fmum.common;

import net.minecraft.util.ResourceLocation;

/**
 * Quick creation of resources in domain of {@value FMUM#MOD_ID}.
 * 
 * @author Giant_Salted_Fish
 */
public final class FMUMResource extends ResourceLocation
{
	public FMUMResource( String path ) {
		super( FMUM.MOD_ID, path );
	}
}
