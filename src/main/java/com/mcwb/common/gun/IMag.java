package com.mcwb.common.gun;

import com.mcwb.common.ammo.IAmmoType;
import com.mcwb.common.operation.IOperationController;

public interface IMag extends IGunPart
{
	public boolean isFull();
	
	public default boolean isEmpty() { return this.ammoCount() == 0; }
	
	public int ammoCount();
	
	public boolean isAllowed( IAmmoType ammo );
	
	public void pushAmmo( IAmmoType ammo );
	
	public default IAmmoType peek() { return this.getAmmo( this.ammoCount() - 1 ); }
	
	public IAmmoType popAmmo();
	
	public IAmmoType getAmmo( int idx );
	
	public IOperationController pushAmmoController();
	
	public IOperationController popAmmoController();
}
