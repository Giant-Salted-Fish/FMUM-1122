package com.fmum.common.gun;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;

public interface IGun< T extends IGunPart< ? extends T > > extends IGunPart< T >
{
	boolean hasMag();
	
	@Nullable
	IMag< ? > mag();
	
	boolean isAllowed( IMag< ? > mag );
	
	void loadMag( IMag< ? > mag );
	
	IMag< ? > unloadMag();
	
	void chargeGun( EntityPlayer player );
}
