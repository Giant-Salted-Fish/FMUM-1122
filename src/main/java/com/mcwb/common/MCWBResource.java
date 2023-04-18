package com.mcwb.common;

import net.minecraft.util.ResourceLocation;

/**
 * for convenience to create resources that belongs to {@link MCWB}.
 * 
 * @author Giant_Salted_Fish
 */
public final class MCWBResource extends ResourceLocation {
	public MCWBResource( String path ) { super( MCWB.MODID, path ); }
}
