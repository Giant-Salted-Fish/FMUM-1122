package com.mcwb.common.gun;

import javax.annotation.Nullable;

public interface IGun extends IGunPart
{
	public default void load( IMag mag ) { this.install( 0, mag ); }
	
	public default IMag unload() { return ( IMag ) this.remove( 0, 0 ); }
	
	@Nullable
	public default IMag mag() { return this.hasMag() ? ( IMag ) this.getInstalled( 0, 0 ) : null; }
	
	public default boolean hasMag() { return this.getInstalledCount( 0 ) > 0; }
}
