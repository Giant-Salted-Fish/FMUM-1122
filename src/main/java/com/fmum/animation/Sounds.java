package com.fmum.animation;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

public final class Sounds
{
	public static final SoundEvent LOAD_AMMO = __createSound( "fmum:load_ammo" );
	public static final SoundEvent UNLOAD_AMMO = __createSound( "fmum:unload_ammo" );
	public static final SoundEvent GUN_FIRE = __createSound( "fmum:gun_fire" );
	public static final SoundEvent GUN_FIRE_SUPPRESSED = __createSound( "fmum:gun_fire_suppressed" );
	public static final SoundEvent SWITCH_FIRE_MODE = __createSound( "fmum:switch_fire_mode" );
	public static final SoundEvent LOAD_MAG = __createSound( "fmum:load_mag" );
	public static final SoundEvent UNLOAD_MAG = __createSound( "fmum:unload_mag" );
	public static final SoundEvent RELEASE_BOLT = __createSound( "fmum:release_bolt" );
	
	private static SoundEvent __createSound( String resource )
	{
		final ResourceLocation res = new ResourceLocation( resource );
		return new SoundEvent( res ).setRegistryName( res );
	}
	
	private Sounds() { }
}
