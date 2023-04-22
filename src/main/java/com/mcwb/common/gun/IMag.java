package com.mcwb.common.gun;

import com.mcwb.common.ammo.IAmmoType;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IMag< T extends IGunPart< ? extends T > > extends IGunPart< T >
{
	public boolean isFull();
	
	public default boolean isEmpty() { return this.ammoCount() == 0; }
	
	public int ammoCount();
	
	public boolean isAllowed( IAmmoType ammo );
	
	public void pushAmmo( IAmmoType ammo );
	
	public IAmmoType popAmmo();
	
	public default IAmmoType peek() { return this.getAmmo( this.ammoCount() - 1 ); }
	
	public IAmmoType getAmmo( int idx );
	
	@SideOnly( Side.CLIENT )
	public boolean isLoadingMag();
	
	@SideOnly( Side.CLIENT )
	public void setAsLoadingMag();
}
