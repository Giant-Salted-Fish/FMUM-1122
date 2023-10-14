package gsf.fmum.common.load;

public class LoaderNotFoundException extends RuntimeException
{
	public LoaderNotFoundException() { }
	
	public LoaderNotFoundException( String msg ) {
		super( msg );
	}
}
