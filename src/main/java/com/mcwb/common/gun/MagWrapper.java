package com.mcwb.common.gun;

import com.mcwb.common.ammo.IAmmoType;

import net.minecraft.item.ItemStack;

public class MagWrapper< I extends IGunPart< ? extends I >, T extends IMag< ? extends I > >
	extends GunPartWrapper< I, T > implements IMag< I >
{
	protected MagWrapper( T primary, ItemStack stack ) { super( primary, stack ); }
	
	@Override
	public boolean isFull() { return this.primary.isFull(); }
	
	@Override
	public int ammoCount() { return this.primary.ammoCount(); }
	
	@Override
	public boolean isAllowed( IAmmoType ammo ) { return this.primary.isAllowed( ammo ); }
	
	@Override
	public void pushAmmo( IAmmoType ammo ) { this.primary.pushAmmo( ammo ); }
	
	@Override
	public IAmmoType popAmmo() { return this.primary.popAmmo(); }
	
	@Override
	public IAmmoType getAmmo( int idx ) { return this.primary.getAmmo( idx ); }
}
