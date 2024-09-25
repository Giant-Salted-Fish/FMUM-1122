package com.fmum.animation;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

public final class Sounds
{
	public static SoundEvent LOAD_AMMO = __newSound( "fmum:load_ammo" );
	public static SoundEvent UNLOAD_AMMO = __newSound( "fmum:unload_ammo" );
	
	private static SoundEvent __newSound( String resource )
	{
		final ResourceLocation res = new ResourceLocation( resource );
		return new SoundEvent( res ).setRegistryName( res );
	}
	
	private Sounds() { }
}
