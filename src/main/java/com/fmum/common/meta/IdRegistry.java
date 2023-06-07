package com.fmum.common.meta;

import com.google.common.collect.HashBiMap;

/**
 * In default the numerical id only has 16-bits of effective range.
 *
 * @author Giant_Salted_Fish
 */
public class IdRegistry< T extends IMeta > extends MetaRegistry< T >
{
	protected final HashBiMap< Integer, T > idMap = HashBiMap.create(); {
		this.idMap.put( 0, null ); // Set 0 to map null.
	}
	
	public final T get( Integer id ) { return this.idMap.get( id ); }
	
	public final Integer getId( T meta ) { return this.idMap.inverse().get( meta ); }
	
	@Override
	public void regis( String name, T meta )
	{
		this.mapper.compute( name, ( key, old ) -> {
			if ( old != null )
			{
				this.logWarning( "fmum.duplicate_meta_regis", old, meta );
				return old;
			}
			
			int id = 0xFFFF & key.hashCode();
			while ( this.idMap.containsKey( id ) ) {
				id += 1;
			}
			this.idMap.put( id, meta );
			return meta;
		} );
	}
}
