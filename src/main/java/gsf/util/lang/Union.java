package gsf.util.lang;

public abstract class Union< T, U >
{
	public abstract T getLeft() throws UnsupportedOperationException;
	
	public abstract U getRight() throws UnsupportedOperationException;
	
	public abstract boolean isLeft();
	
	public abstract boolean isRight();
	
	
	public static < T, U > Union< T, U > fromLeft( T value )
	{
		return new Union< T, U >() {
			@Override
			public T getLeft() {
				return value;
			}
			
			@Override
			public U getRight() {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public boolean isLeft() {
				return true;
			}
			
			@Override
			public boolean isRight()
			{
				return false;
			}
		};
	}
	
	public static < T, U > Union< T, U > fromRight( U value )
	{
		return new Union< T, U >() {
			@Override
			public T getLeft() throws UnsupportedOperationException {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public U getRight() {
				return value;
			}
			
			@Override
			public boolean isLeft() {
				return false;
			}
			
			@Override
			public boolean isRight()
			{
				return true;
			}
		};
	}
}
