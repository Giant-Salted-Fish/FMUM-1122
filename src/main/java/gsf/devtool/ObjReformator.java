package gsf.devtool;

import net.minecraftforge.client.model.obj.OBJLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p> Do two things: </p>
 * <ol>
 *     <li> Ensure all "vt" value is inside of {@code 0.0-1.0} (required by {@link OBJLoader}. </li>
 *     <li> Comment "mtllib" and "usemtl" if represent. </li>
 * </ol>
 * 
 * @author Giant_Salted_Fish
 */
public final class ObjReformator
{
	public static void main( String[] args )
	{
		final String src_dir_path = args.length < 1 ? "tmp-dev/" : args[ 0 ];
		final String dst_dir_path = args.length < 2 ? src_dir_path + "dst/" : args[ 1 ];
		
		System.out.println( "Src Dir: " + src_dir_path );
		System.out.println( "Dst Dir: " + dst_dir_path );
		
		final File dst_dir = new File( dst_dir_path );
		if ( !dst_dir.exists() && !dst_dir.mkdirs() ) {
			throw new RuntimeException( "Failed to create output directory" );
		}
		
		Arrays.stream( Objects.requireNonNull( new File( src_dir_path ).listFiles() ) )
			.filter( f -> f.getName().endsWith( ".obj" ) )
			.forEach( f -> {
				// If this file has .obj.obj suffix then remove it.
				final String file_name = f.getName();
				final String dst_file_name = (
					file_name.endsWith( ".obj.obj" )
					? file_name.substring( 0, file_name.length() - 4 )
					: file_name
				);
				process( f, new File( dst_dir, dst_file_name ) );
				System.out.println( "Complete " + file_name );
			} );
		System.out.println( "All done" );
	}
	
	private static void process( File src, File dst )
	{
		try
		{
			final Iterable< String > lines = (
				Files.readAllLines( src.toPath() ).stream()
				.map( line -> {
					// Remove "mtllib" and "usemtl" label
					if ( line.startsWith( "mtllib " ) || line.startsWith( "usemtl" ) ) {
						return '#' + line;
					}
					
					// Invert texture coordinate and ensure its value inside range {0.0-1.0}.
					if ( line.startsWith( "vt " ) )
					{
						final double[] values = (
							Arrays.stream( line.split( " " ) )
							.skip( 1 )  // Skip "vt" tag.
							.mapToDouble( Double::parseDouble )
							.map( v -> v - Math.floor( v ) )
							.toArray()
						);
						assert values.length == 2;
						values[ 1 ] = 1.0 - values[ 1 ];
						return "vt " + (
							Arrays.stream( values )
							.mapToObj( Double::toString )
							.collect( Collectors.joining( " " ) )
						);
					}
					
					// Swap x and z axis.
					if ( line.startsWith( "v " ) )
					{
						final double[] values = (
							Arrays.stream( line.split( " " ) )
							.skip( 1 )  // Skip "v" tag.
							.mapToDouble( Double::parseDouble )
							.toArray()
						);
						
						final double ori_x = values[ 0 ];
						final double ori_z = values[ 2 ];
						values[ 0 ] = -ori_z;
						values[ 2 ] = ori_x;
						
						return "v " + (
							Arrays.stream( values )
							.mapToObj( Double::toString )
							.collect( Collectors.joining( " " ) )
						);
					}
					
					return line;
				} )
				::iterator
			);
			Files.write( dst.toPath(), lines );
		}
		catch ( IOException e ) {
			throw new RuntimeException( e );
		}
	}
}
