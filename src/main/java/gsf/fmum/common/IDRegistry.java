package gsf.fmum.common;

import com.google.common.collect.HashBiMap;

import java.util.Optional;
import java.util.function.Function;

/**
 * In default the numerical id only has 16-bits of effective range.
 */
public class IDRegistry< T > extends Registry< T >
{
	protected final HashBiMap< Integer, T > id_map_table = HashBiMap.create();
	
	public IDRegistry() { }
	
	public IDRegistry( Function< T, String > identifier_retriever ) {
		super( identifier_retriever );
	}
	
	public final T get( Integer id )
	{
		final T value = this.id_map_table.get( id );
		assert value != null;
		return value;
	}
	
	public final Optional< T > lookup( Integer id ) {
		return Optional.ofNullable( this.id_map_table.get( id ) );
	}
	
	public final Integer getID( T value ) {
		return this.id_map_table.inverse().get( value );
	}
	
	@Override
	public void regis( String identifier, T value )
	{
		this.regis_table.compute( identifier, ( key, old_value ) -> {
			final boolean is_already_registered = old_value != null;
			if ( is_already_registered )
			{
				FMUM.MOD.logWarning(
					"fmum.duplicate_content_regis", old_value, value );
				return old_value;
			}
			
			// Compute an ID that has not been occupied.
			int id = 0xFFFF & key.hashCode();
			while ( this.id_map_table.containsKey( id ) ) {
				id += 1;
			}
			
			this.id_map_table.put( id, value );
			return value;
		} );
	}
}
