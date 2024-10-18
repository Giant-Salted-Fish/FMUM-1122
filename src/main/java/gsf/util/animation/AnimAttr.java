package gsf.util.animation;

import com.fmum.load.JsonData;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;

public abstract class AnimAttr< T >
{
	@SuppressWarnings( "unchecked" )
	public < U > T cast( U value ) {
		return ( T ) value;
	}
	
	public abstract Optional< T > compose( @Nullable T left, @Nullable T right );
	
	public abstract Optional< Function< Float, T > > parse( JsonData data, float anim_len );
}
