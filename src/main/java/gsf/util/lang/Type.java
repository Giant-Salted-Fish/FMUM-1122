package gsf.util.lang;

import java.util.Optional;

public final class Type
{
	public static < T > Optional< T > cast( Object obj, Class< T > type ) {
		return type.isInstance( obj ) ? Optional.of( type.cast( obj ) ) : Optional.empty();
	}
	
	private Type() { }
}
