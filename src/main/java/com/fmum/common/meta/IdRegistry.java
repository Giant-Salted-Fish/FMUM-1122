package com.fmum.common.meta;

import com.google.common.collect.HashBiMap;

import java.util.function.Function;

public class IdRegistry< T extends IMeta > extends Registry< T >
{
	protected final Function< T, Integer > idGenerator;
	
	protected final HashBiMap< Integer, T > idMap = HashBiMap.create();
	
	public IdRegistry() { this( meta -> 0xFFFF & meta.name().hashCode() ); }
	
	public IdRegistry( Function< T, Integer > idGenerator ) {
		this.idGenerator = idGenerator;
	}
	
	public final T get( Integer id ) { return this.idMap.get( id ); }
	
	public final Integer getId( T meta ) { return this.idMap.inverse().get( meta ); }
	
	@Override
	public void put( String name, T meta )
	{
		this.mapper.compute( name, ( key, old ) -> {
			if ( old != null )
			{
				this.logWarning( "fmum.duplicate_meta_regis", old, meta );
				return old;
			}
			
			Integer id = this.idGenerator.apply( meta );
			while ( this.idMap.containsKey( id ) ) {
				id += 1;
			}
			this.idMap.put( id, meta );
			return meta;
		} );
	}
}
