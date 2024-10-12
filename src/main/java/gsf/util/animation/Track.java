package gsf.util.animation;

import java.util.Arrays;

public final class Track< T >
{
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
}
