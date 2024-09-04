package com.fmum;

import com.google.common.collect.HashBiMap;

import java.util.Optional;

public abstract class BiRegistry< K, V > extends Registry< V >
{
	protected final HashBiMap< K, V > id_table = HashBiMap.create();
	
	
	public static < V > BiRegistry< Short, V > createWithShortKey()
	{
		return new BiRegistry< Short, V >() {
			@Override
			protected void _doBiRegis( V obj )
			{
				// Compute a numerical id that has not been occupied.
				short id = ( short ) obj.hashCode();
				while ( id == 0 || this.id_table.containsKey( id ) ) {
					id += 1;
				}
				this.id_table.put( id, obj );
			}
		};
	}
	
	
	public final Optional< V > lookup( K key ) {
		return Optional.ofNullable( this.id_table.get( key ) );
	}
	
	public final Optional< K > lookupID( V obj ) {
		return Optional.ofNullable( this.id_table.inverse().get( obj ) );
	}
	
	@Override
	public void regis( String regis_name, V obj )
	{
		this.regis_table.compute( regis_name, ( key, old_value ) -> {
			final boolean is_already_registered = old_value != null;
			if ( is_already_registered )
			{
				FMUM.LOGGER.warn( "fmum.duplicate_content_regis", old_value, obj );
				return old_value;
			}
			
			this._doBiRegis( obj );
			return obj;
		} );
	}
	
	protected abstract void _doBiRegis( V obj );
}
