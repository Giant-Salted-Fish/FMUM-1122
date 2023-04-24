package com.mcwb.common.gun;

import javax.annotation.Nullable;

public interface IGun< T extends IGunPart< ? extends T > > extends IGunPart< T >
{
	boolean hasMag();
	
	@Nullable
	IMag< ? > mag();
	
	boolean isAllowed( IMag< ? > mag );
	
	void loadMag( IMag< ? > mag );
	
	IMag< ? > unloadMag();
}
