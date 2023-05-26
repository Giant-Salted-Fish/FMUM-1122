package com.fmum.common.gun;

import com.fmum.common.ammo.IAmmoType;
import com.fmum.common.mag.IMag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.function.Consumer;

public class GunWrapper< I extends IGunPart< ? extends I >, T extends IGun< ? extends I > >
	extends GunPartWrapper< I, T > implements IGun< I >
{
	protected GunWrapper( T primary, ItemStack stack ) { super( primary, stack ); }
	
	@Override
	public boolean hasMag() { return this.primary.hasMag(); }
	
	@Override
	public IMag< ? > mag() { return this.primary.mag(); }
	
	@Override
	public boolean isAllowed( IMag< ? > mag ) { return this.primary.isAllowed( mag ); }
	
	@Override
	public void loadMag( IMag< ? > mag ) { this.primary.loadMag( mag ); }
	
	@Override
	public IMag< ? > unloadMag() { return this.primary.unloadMag(); }
	
	@Override
	public void chargeGun( EntityPlayer player ) { this.primary.chargeGun( player ); }

	@Override
	public void forEachAmmo( Consumer< IAmmoType > visitor ) {
		this.primary.forEachAmmo( visitor );
	}
}
