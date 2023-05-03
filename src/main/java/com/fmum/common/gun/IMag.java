package com.fmum.common.gun;

import java.util.function.Consumer;

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
	
	IAmmoType peekAmmo();
	
	void forEachAmmo( Consumer< IAmmoType > visitor );
	
	@SideOnly( Side.CLIENT )
	boolean isLoadingMag();
	
	@SideOnly( Side.CLIENT )
	void setAsLoadingMag();
}
