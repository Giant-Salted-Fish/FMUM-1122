package com.fmum.common.gun;

import com.fmum.common.ammo.IAmmoType;
import com.fmum.common.mag.IMag;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public interface IGun< T extends IGunPart< ? extends T > > extends IGunPart< T >
{
	boolean hasMag();
	
	@Nullable
	IMag< ? > mag();
	
	boolean isAllowed( IMag< ? > mag );
	
	void loadMag( IMag< ? > mag );
	
	IMag< ? > unloadMag();
	
	void chargeGun( EntityPlayer player );
	
	void forEachAmmo( Consumer< IAmmoType > visitor );
}
