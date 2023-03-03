package com.mcwb.common.gun;

import com.mcwb.common.ammo.IAmmoType;
import com.mcwb.common.operation.IOperationController;

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
	
	public IOperationController pushAmmoController();
	
	public IOperationController popAmmoController();
}
