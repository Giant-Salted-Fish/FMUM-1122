package gsf.util.animation;

import javax.annotation.Nullable;
import java.util.Optional;

public abstract class AnimAttr< T >
{
	@SuppressWarnings( "unchecked" )
	public < U > T cast( U value ) {
		return ( T ) value;
	}
	
	public abstract Optional< T > compose( @Nullable T left, @Nullable T right );
}
