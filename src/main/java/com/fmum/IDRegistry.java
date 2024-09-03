package com.fmum;

import com.google.common.collect.HashBiMap;

import java.util.Optional;

public class IDRegistry< T > extends Registry< T >
{
	protected final HashBiMap< Short, T > id_table = HashBiMap.create();
	
	
	public final Optional< T > lookup( Short id ) {
		return Optional.ofNullable( this.id_table.get( id ) );
	}
	
	public final Optional< Short > lookupID( T obj ) {
		return Optional.ofNullable( this.id_table.inverse().get( obj ) );
	}
	
	@Override
	public void regis( String regis_name, T obj )
	{
		this.regis_table.compute( regis_name, ( key, old_value ) -> {
			final boolean is_already_registered = old_value != null;
			if ( is_already_registered )
			{
				FMUM.LOGGER.warn( "fmum.duplicate_content_regis", old_value, obj );
				return old_value;
			}
			
			// Compute a numerical id that has not been occupied.
			short id = ( short ) key.hashCode();
			while ( this.id_table.containsKey( id ) ) {
				id += 1;
			}
			
			this.id_table.put( id, obj );
			return obj;
		} );
	}
}
