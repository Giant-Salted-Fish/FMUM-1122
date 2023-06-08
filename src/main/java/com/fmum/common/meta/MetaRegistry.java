package com.fmum.common.meta;

import com.fmum.common.FMUM;
import com.fmum.common.Registry;

public class MetaRegistry< T extends IMeta > extends Registry< T >
{
	public void regis( T meta ) { this.regis( meta.name(), meta ); }
	
	@Override
	public void regis( String name, T meta )
	{
		this.mapper.compute( name, ( key, old ) -> {
			if ( old == null ) { return meta; }
			
			FMUM.logWarning( "fmum.duplicate_meta_regis", old, meta );
			return old;
		} );
	}
}
