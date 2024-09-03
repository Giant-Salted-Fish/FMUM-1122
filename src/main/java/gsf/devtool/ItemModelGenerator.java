package gsf.devtool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

// TODO: No longer need to generate item model maybe?
@Deprecated
public final class ItemModelGenerator
{
	public static void main( String[] args )
	{
		final String src_dir_path = args.length < 1 ? "z-dev/src/" : args[ 0 ];
		final String dst_dir_path = args.length < 2 ? "z-dev/dst/" : args[ 1 ];
		
		processDir( new File( src_dir_path ), new File( dst_dir_path ) );
	}
	
	private static void processDir( File src_dir, File dst_dir )
	{
		Arrays.stream( Objects.requireNonNull( src_dir.listFiles() ) ).forEachOrdered( file -> {
			final String fName = file.getName();
			
			if ( file.isDirectory() ) {
				processDir( file, dst_dir );
			}
			else if ( fName.endsWith( ".json" ) )
			{
				final File dstFile = new File( dst_dir, fName );
				if ( dstFile.exists() ) {
					System.out.println( "Skipped file " + fName );
				}
				else
				{
					try ( BufferedWriter out = new BufferedWriter( new FileWriter( dstFile ) ) )
					{
						out.write(
							"{\n" +
							"\t" + "\"parent\": \"builtin/generated\",\n" +
							"\t" + "\"textures\": {\n" +
							"\t" + "\t" + "\"layer0\": \"fmum:items/" + fName.substring( 0, fName.length() - 5 ) + "\"\n" +
							"\t" + "}\n" +
							"}\n"
						);
					}
					catch ( IOException e ) {
						throw new RuntimeException( e );
					}
				}
				System.out.println( "Gen item model for " + file.getName() );
			}
			else System.out.println( "Unrecognized file " + file.getName() );
		} );
	}
}
