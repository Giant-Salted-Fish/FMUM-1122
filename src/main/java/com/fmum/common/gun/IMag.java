package com.fmum.common.gun;

import com.fmum.common.ammo.IAmmoType;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IMag< T extends IGunPart< ? extends T > > extends IGunPart< T >
{
	boolean isFull();
	
	default boolean isEmpty() { return this.ammoCount() == 0; }
	
	int ammoCount();
	
	boolean isAllowed( IAmmoType ammo );
	
	void pushAmmo( IAmmoType ammo );
	
	IAmmoType popAmmo();
	
	default IAmmoType peek() { return this.getAmmo( this.ammoCount() - 1 ); }
	
	IAmmoType getAmmo( int idx );
	
	@SideOnly( Side.CLIENT )
	boolean isLoadingMag();
	
	@SideOnly( Side.CLIENT )
	void setAsLoadingMag();
}
