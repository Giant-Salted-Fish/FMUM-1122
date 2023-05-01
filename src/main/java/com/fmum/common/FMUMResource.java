package com.fmum.common;

import net.minecraft.util.ResourceLocation;

/**
 * for convenience to create resources that belongs to {@link FMUM}.
 * 
 * @author Giant_Salted_Fish
 */
public final class FMUMResource extends ResourceLocation {
	public FMUMResource( String path ) { super( FMUM.MODID, path ); }
}
