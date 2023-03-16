package com.mcwb.common.gun;

import javax.annotation.Nullable;

public interface IGun< T extends IGunPart< ? extends T > > extends IGunPart< T >
{
	public boolean hasMag();
	
	@Nullable
	public IMag< ? > mag();
	
	public boolean isAllowed( IMag< ? > mag );
	
	public void loadMag( IMag< ? > mag );
	
	public IMag< ? > unloadMag();
}
