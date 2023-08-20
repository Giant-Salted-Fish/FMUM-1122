package com.fmum.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class Registry< T >
{
	protected final HashMap< String, T > regis_table = new HashMap<>();
	protected final Function< T, String > identifier_retriever;
	
	public Registry() {
		this( value -> { throw new RuntimeException(); } );
	}
	
	public Registry( Function< T, String > identifier_retriever ) {
		this.identifier_retriever = identifier_retriever;
	}
	
	public final T get( String identifier ) {
		return Objects.requireNonNull( this.regis_table.get( identifier ) );
	}
	
	public final Optional< T > lookup( String identifier ) {
		return Optional.ofNullable( this.regis_table.get( identifier ) );
	}
	
	public final Collection< T > values() {
		return this.regis_table.values();
	}
	
	public final int size() {
		return this.regis_table.size();
	}
	
	public void regis( T value )
	{
		final String identifier = this.identifier_retriever.apply( value );
		this.regis( identifier, value );
	}
	
	public void regis( String identifier, T value )
	{
		this.regis_table.compute( identifier, ( key, old_value ) -> {
			final boolean is_already_registered = old_value != null;
			if ( is_already_registered )
			{
				FMUM.MOD.logWarning(
					"fmum.duplicate_content_regis", old_value, value );
			}
			
			// Do not override old value if already existed.
			return is_already_registered ? old_value : value;
		} );
	}
}
