package com.fmum;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

public class Registry< T >
{
	protected final HashMap< String, T > regis_table = new HashMap<>();
	
	
	public final Optional< T > lookup( String regis_name ) {
		return Optional.ofNullable( this.regis_table.get( regis_name ) );
	}
	
	public final Collection< T > values() {
		return this.regis_table.values();
	}
	
	public final int size() {
		return this.regis_table.size();
	}
	
	public void regis( String regis_name, T obj )
	{
		this.regis_table.compute( regis_name, ( rn, old_obj ) -> {
			final boolean is_already_registered = old_obj != null;
			if ( !is_already_registered ) {
				return obj;
			}
			
			FMUM.LOGGER.warn( "fmum.duplicate_content_regis", old_obj, obj );
			return old_obj;  // Keep old one if already existed.
		} );
	}
}
