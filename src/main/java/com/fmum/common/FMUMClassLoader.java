package com.fmum.common;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;

import com.fmum.common.util.Util;

public final class FMUMClassLoader extends URLClassLoader implements AutowireLogger
{
	public static final FMUMClassLoader INSTANCE = new FMUMClassLoader();
	
	private final HashMap< String, byte[] > source = new HashMap<>();
	
	private FMUMClassLoader() {
		super( new URL[ 0 ], net.minecraft.server.MinecraftServer.class.getClassLoader() );
	}
	
	public byte[] putSource( String key, byte[] value ) {
		return this.source.put( key, value );
	}
	
	public void clear() { source.clear(); }
	
	/**
	 * @return {@code null} if an error has occurred
	 */
	public Object tryInstantiate( String... pathFragments )
	{
		try
		{
			return this.loadClass(
				pathFragments[ 0 ] = Util.spliceClassPath( pathFragments )
			).getConstructor().newInstance();
		}
		catch( Exception e ) {
			this.log().error( this.format( "fmum.errorinstantiating", pathFragments[ 0 ] ), e );
		}
		return null;
	}
	
	@Override
	public void addURL( URL url ) { super.addURL( url ); }
	
	@Override
	protected Class<?> findClass( String name ) throws ClassNotFoundException
	{
		byte[] bytes = this.source.get( name );
		return(
			bytes != null
			? this.defineClass( name, bytes, 0, bytes.length )
			: super.findClass( name )
		);
	}
}
