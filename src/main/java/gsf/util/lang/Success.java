package gsf.util.lang;

import java.util.function.Consumer;
import java.util.function.Function;

public class Success< T, E > extends Result< T, E >
{
	private Object value;
	
	public Success( T value ) {
		this.value = value;
	}
	
	@Override
	@SuppressWarnings( "unchecked" )
	public T unwrap() {
		return ( T ) this.value;
	}
	
	@Override
	public E getCause() throws UnsupportedOperationException {
		throw new UnsupportedOperationException( "Result is success." );
	}
	
	@Override
	@SuppressWarnings( "unchecked" )
	public < U > Result< U, E > map( Function< ? super T, ? extends U > mapper )
	{
		this.value = mapper.apply( this.unwrap() );
		return ( Result< U, E > ) this;
	}
	
	@Override
	public < U > Result< U, E > flatMap( Function< ? super T, Result< U, E > > mapper ) {
		return mapper.apply( this.unwrap() );
	}
	
	@Override
	public boolean isSuccess() {
		return true;
	}
	
	@Override
	public void ifSuccess( Consumer< ? super T > handler ) {
		handler.accept( this.unwrap() );
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
	
	@Override
	public T orElse( T other ) {
		return this.unwrap();
	}
	
	@Override
	public T orElseGet( Function< ? super E, ? extends T > handler ) {
		return this.unwrap();
	}
	
	@Override
	public T orElseThrow( Function< ? super E, ? extends RuntimeException > factory ) {
		return this.unwrap();
	}
}
