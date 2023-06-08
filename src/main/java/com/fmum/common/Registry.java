package com.fmum.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Supplier;

public class Registry< T >
{
	protected final HashMap< String, T > mapper = new HashMap<>();
	
	public final T get( String key ) { return this.mapper.get( key ); }
	
	public final T getOrElse( String key, Supplier< ? extends T > other ) {
		return Optional.ofNullable( this.mapper.get( key ) ).orElseGet( other );
	}
	
	public final Collection< T > values() { return this.mapper.values(); }
	
	public void regis( String name, T meta )
	{
		this.mapper.compute( name, ( key, old ) -> {
			if ( old == null ) { return meta; }
			
			FMUM.logWarning( "fmum.duplicate_regis", old, meta );
			return old;
		} );
	}
	
	public final int size() { return this.mapper.size(); }
}
