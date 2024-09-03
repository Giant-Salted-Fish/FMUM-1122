package gsf.util.math;

public final class MoreMath
{
	public static final float PI = ( float ) Math.PI;
	
	public static float toRadians( float angle )
	{
		final float angle_2_radians = PI / 180.0F;
		return angle * angle_2_radians;
	}
	
	public static float toDegrees( float radian )
	{
		final float radian_2_angle = 180.0F / PI;
		return radian * radian_2_angle;
	}
	
	public static float lerp( float a, float b, float alpha ) {
		return ( 1.0F - alpha ) * a + alpha * b;
	}
	
	/**
	 * @return {@code (a + shift) / (b + shift)}, {@code 0.0F} if {@code b + shift == 0.0F}.
	 */
	public static float shiftDiv( float a, float b, float shift )
	{
		float divisor = b + shift;
		return divisor != 0.0F ? ( a + shift ) / divisor : 0.0F;
	}
	
	public static float fastInvSqrt( float value )
	{
		final float half = 0.5F * value;
		int i = Float.floatToRawIntBits( value );
		i = 0x5f3759df - ( i >> 1 );
		value = Float.intBitsToFloat( i );
		return value * ( 1.5F - half * value * value );
	}
	
	private MoreMath() { }
}
