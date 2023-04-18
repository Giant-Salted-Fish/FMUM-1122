package com.mcwb.common.meta;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Supplier;

import com.mcwb.common.IAutowireLogger;

public final class Registry< T extends IMeta > implements IAutowireLogger
{
	private final HashMap< String, T > mapper = new HashMap< String, T >();
	
	public T get( String key ) { return this.mapper.get( key ); }
	
	public T getOrElse( String key, Supplier< ? extends T > other ) {
		return Optional.ofNullable( this.mapper.get( key ) ).orElseGet( other );
	}
	
	public Collection< T > values() { return this.mapper.values(); }
	
	public void regis( T meta )
	{
		this.mapper.compute( meta.name(), ( key, old ) -> {
			if ( old == null ) { return meta; }
			
			this.logWarning( "mcwb.duplicate_meta_regis", old, meta );
			return old;
		} );
	}
	
	public int size() { return this.mapper.size(); }
}
