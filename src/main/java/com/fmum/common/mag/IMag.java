package com.fmum.common.mag;

import com.fmum.common.ammo.IAmmoType;
import com.fmum.common.gun.IGunPart;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Consumer;

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
