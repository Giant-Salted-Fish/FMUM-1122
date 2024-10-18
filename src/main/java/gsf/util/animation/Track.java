package gsf.util.animation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gsf.util.math.Quat4f;
import gsf.util.math.Vec3f;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.IntFunction;

public final class Track< T >
{
	public static final Track< Vec3f >
		EMPTY_POS_TRACK = new Track<>( new float[] { 0.0F }, new Vec3f[] { Vec3f.ORIGIN } );
	public static final Track< Quat4f >
		EMPTY_ROT_TRACK = new Track<>( new float[] { 0.0F }, new Quat4f[] { Quat4f.IDENTITY } );
	public static final Track< Float >
		EMPTY_ALPHA_TRACK = new Track<>( new float[] { 0.0F }, new Float[] { 0.0F } );
	
	
	private final float[] keys;
	private final T[] values;
	
	public Track( float[] keys, T[] values )
	{
		assert keys.length > 0;
		assert keys.length == values.length;
		this.keys = keys;
		this.values = values;
	}
	
	public T findFloor( float key )
	{
		final int result = Arrays.binarySearch( this.keys, key );
		return this.values[ result < 0 ? Math.max( 0, -result - 2 ) : result ];
	}
	
	public T findCeiling( float key )
	{
		final int result = Arrays.binarySearch( this.keys, key );
		return this.values[ result < 0 ? Math.min( this.keys.length - 1, -result - 1 ) : result ];
	}
	
	public < U > U lerp( float key, LerpFunc< ? super T, ? extends U > func )
	{
		final float[] keys = this.keys;
		final T[] values = this.values;
		final int result = Arrays.binarySearch( keys, key );
		final int idx = result < 0 ? -result - 1 : result;
		final int floor = Math.max( 0, idx - 1 );
		final int ceiling = Math.min( keys.length - 1, idx );
		final float shift = keys[ floor ];
		final float divisor = keys[ ceiling ] - shift;
		final float alpha = divisor > 0.0F ? ( key - shift ) / divisor : 0.0F;
		return func.lerp( values[ floor ], values[ ceiling ], alpha );
	}
	
	@FunctionalInterface
	public interface LerpFunc< T, R > {
		R lerp( T a, T b, float alpha );
	}
	
	
	public static < T > Track< T > from(
		JsonObject obj,
		float key_scale,
		IntFunction< T[] > factory,
		Function< ? super JsonElement, ? extends T > parser
	) {
		final int size = obj.size();
		final float[] keys = new float[ size ];
		final T[] values = factory.apply( size );
		
		int idx = 0;
		for ( Entry< String, JsonElement > entry : obj.entrySet() )
		{
			final String key = entry.getKey();
			final JsonElement value = entry.getValue();
			keys[ idx ] = key_scale * Float.parseFloat( key );
			values[ idx ] = parser.apply( value );
			idx += 1;
		}
		return new Track<>( keys, values );
	}
}
