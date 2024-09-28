package com.fmum.mag;

import com.fmum.ammo.IAmmoType;
import com.fmum.gunpart.IGunPart;
import com.fmum.item.IItem;
import com.fmum.module.IModule;
import gsf.util.lang.Result;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;
import java.util.function.IntSupplier;

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
	
//	boolean isCompatibleAmmo( IAmmoType ammo );
	
	Result< IntSupplier, String > checkAmmoForLoad( IAmmoType ammo );
	
	IAmmoType popAmmo();
	
//	IAmmoType getAmmo( int idx );
	
	Optional< ? extends IAmmoType > peekAmmo();
	
	@SideOnly( Side.CLIENT )
	IMag IMag$createLoadingProxy();
	
	
	static IMag from( IItem item )
	{
		final Optional< IModule > opt = item.lookupCapability( IModule.CAPABILITY );
		return ( IMag ) opt.orElseThrow( IllegalAccessError::new );
	}
}
