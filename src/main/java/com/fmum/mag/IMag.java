package com.fmum.mag;

import com.fmum.ammo.IAmmoType;
import com.fmum.gunpart.IGunPart;

import java.util.Optional;

public interface IMag extends IGunPart
{
	int getCapacity();
	
	int getAmmoCount();
	
	default boolean isEmpty() {
		return this.getAmmoCount() == 0;
	}
	
	default boolean isFull() {
		return this.getAmmoCount() == this.getCapacity();
	}
	
	boolean canLoadAmmo( IAmmoType ammo );
	
	int loadAmmo( IAmmoType ammo );
	
	IAmmoType popAmmo();
	
//	IAmmoType getAmmo( int idx );
	
	Optional< ? extends IAmmoType > peekAmmo();
}
