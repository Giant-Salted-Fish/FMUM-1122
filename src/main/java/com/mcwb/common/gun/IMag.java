package com.mcwb.common.gun;

import com.mcwb.common.ammo.IAmmoType;

public interface IMag extends IGunPart
{
	public boolean isFull();
	
	public default boolean isEmpty() { return this.ammoCount() == 0; }
	
	public int ammoCount();
	
	public boolean isAllowed( IAmmoType ammo );
	
	public void push( IAmmoType ammo );
	
	public default IAmmoType peek() { return this.get( this.ammoCount() - 1 ); }
	
	public IAmmoType pop();
	
	public IAmmoType get( int idx );
}
