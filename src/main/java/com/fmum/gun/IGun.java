package com.fmum.gun;

import com.fmum.gunpart.IGunPart;
import com.fmum.item.IItem;
import com.fmum.mag.IMag;
import com.fmum.module.IModule;
import gsf.util.lang.Result;

import java.util.Optional;

public interface IGun extends IGunPart
{
	Optional< ? extends IMag > getMag();
	
	Result< Runnable, String > checkMagForLoad( IMag mag );
	
	IMag popMag();
	
	
	static IGun from( IItem item )
	{
		final Optional< IModule > opt = item.lookupCapability( IModule.CAPABILITY );
		return ( IGun ) opt.orElseThrow( IllegalArgumentException::new );
	}
}
