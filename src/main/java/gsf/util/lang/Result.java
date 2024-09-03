package gsf.util.lang;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Result< T, E >
{
	public static < T > Result< T, Exception > of( IRunOnce< ? extends T > expression )
	{
		T value;
		try {
			value = expression.run();
		}
		catch ( Exception e ) {
			return new Error<>( e );
		}
		return new Success<>( value );
	}
	
	@FunctionalInterface
	public interface IRunOnce< T > {
		T run() throws Exception;
	}
	
	public abstract T unwrap() throws UnsupportedOperationException;
	
	public abstract E getCause() throws UnsupportedOperationException;
	
	public abstract < U > Result< U, E > map( Function< ? super T, ? extends U > mapper );
	
	public abstract < U > Result< U, E > flatMap( Function< ? super T, Result< U, E > > mapper );
	
	public abstract boolean isSuccess();
	
	public abstract void ifSuccess( Consumer< ? super T > handler );
	
	public abstract Function< Consumer< ? super E >, Result< T, E > > matchError( Class< ? >... e_class );
	
	public abstract Result< T, E > matchAnyError( Consumer< ? super E > action );
	
	public abstract void exhaustive() throws UnsupportedOperationException;
	
	public abstract T orElse( T other );
	
	public abstract T orElseGet( Function< ? super E, ? extends T > handler );
	
	public abstract T orElseThrow(
		Function< ? super E, ? extends RuntimeException > factory
	);
}
