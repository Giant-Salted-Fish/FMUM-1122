package gsf.util.lang;


import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

public class Error< T, E > extends Result< T, E >
{
	private final E e;
	
	public Error( E e ) {
		this.e = e;
	}
	
	@Override
	public T unwrap() throws UnsupportedOperationException {
		throw new UnsupportedOperationException( "Result is error: " + this.e );
	}
	
	@Override
	public E getCause() {
		return this.e;
	}
	
	@Override
	@SuppressWarnings( "unchecked" )
	public < U > Result< U, E > map( Function< ? super T, ? extends U > mapper ) {
		return ( Result< U, E > ) this;
	}
	
	@Override
	@SuppressWarnings( "unchecked" )
	public < U > Result< U, E > flatMap( Function< ? super T, Result< U, E > > mapper ) {
		return ( Result< U, E > ) this;
	}
	
	@Override
	public boolean isSuccess() {
		return false;
	}
	
	@Override
	public void ifSuccess( Consumer< ? super T > handler ) {
		// Pass.
	}
	
	@Override
	public Function< Consumer< ? super E >, Result< T, E > > matchError( Class< ? >... e_class )
	{
		return (
			Arrays.stream( e_class ).anyMatch( c -> c.isInstance( this.e ) )
			? action -> {
				action.accept( this.e );
				return new Exhausted<>( this );
			}
			: action -> this
		);
	}
	
	@Override
	public Result< T, E > matchAnyError( Consumer< ? super E > action )
	{
		action.accept( this.e );
		return new Exhausted<>( this );
	}
	
	@Override
	public void exhaustive() throws UnsupportedOperationException {
		throw new UnsupportedOperationException( "Error is not exhausted: " + this.e );
	}
	
	@Override
	public T orElse( T other ) {
		return other;
	}
	
	@Override
	public T orElseGet( Function< ? super E, ? extends T > handler ) {
		return handler.apply( this.e );
	}
	
	@Override
	public T orElseThrow( Function< ? super E, ? extends RuntimeException > factory ) {
		throw factory.apply( this.e );
	}
	
	
	/**
	 * An {@link Error} that has been fully matched.
	 *
	 * @see Result#matchError(Class...)
	 */
	public static class Exhausted< T, E > extends Error< T, E >
	{
		public Exhausted( Error< T, E > error ) {
			super( error.e );
		}
		
		@Override
		public Function< Consumer< ? super E >, Result< T, E > > matchError( Class< ? >... e_class ) {
			return action -> this;
		}
		
		@Override
		public Result< T, E > matchAnyError( Consumer< ? super E > action ) {
			return this;
		}
		
		@Override
		public void exhaustive() {
			// Pass.
		}
	}
}
